{
  pkgs,
  inputs,
}: let
  zedSettings = {
    lsp = {
      nix = {
        binary = {
          path_lookup = true;
        };
      };
      nil = {
        initialization_options = {
          formatting = {
            command = [
              "alejandra"
              "--quiet"
              "--"
            ];
          };
        };
      };
      nixd = {
        initialization_options = {
          formatting = {
            command = [
              "alejandra"
              "--quiet"
              "--"
            ];
          };
        };
      };
    };

    auto_install_extensions = {
      "nix" = true;
      "java" = true;
      "kotlin" = true;
    };

    languages = {
      nix = {
        formatter = {
          external = {
            command = "alejandra";
            arguments = [
              "--quiet"
              "--"
            ];
          };
        };
      };
    };
  };
  snbt-merge = pkgs.writeShellScriptBin "snbt-merge" ''
    exec ${pkgs.python3}/bin/python3 ${./scripts/snbt_merge.py} "$@"
  '';
  gcommit = pkgs.writeShellScriptBin "gcommit" ''
    msg_file="GIT_COMMIT_MSG"

    if [[ ! -f "$msg_file" ]] || [[ ! -s "$msg_file" ]]; then
      echo "Error: $msg_file is missing or empty. Nothing to commit." >&2
      exit 1
    fi

    echo ""
    echo "=== Commit message (from $msg_file) ==="
    cat "$msg_file"
    echo "========================================"
    echo ""
    read -r -p "Commit with this message? [y/N] " gc_confirm
    if [[ "$gc_confirm" != "y" && "$gc_confirm" != "Y" ]]; then
      echo "Aborted — $msg_file left unchanged."
      exit 0
    fi

    git commit -F "$msg_file"
    gc_exit=$?
    if [[ $gc_exit -ne 0 ]]; then
      echo "Commit failed (exit $gc_exit). $msg_file left unchanged." >&2
      exit $gc_exit
    fi

    echo ""
    read -r -p "Tag this commit? [y/N] " gc_do_tag
    if [[ "$gc_do_tag" == "y" || "$gc_do_tag" == "Y" ]]; then
      read -r -p "Tag name (e.g. v1.2.0): " gc_tag_name
      if [[ -z "$gc_tag_name" ]]; then
        echo "No tag name given — skipping tag."
      else
        read -r -p "Tag annotation (leave blank to reuse commit message): " gc_tag_msg
        if [[ -z "$gc_tag_msg" ]]; then
          git tag -s "$gc_tag_name" -F "$msg_file"
        else
          git tag -s "$gc_tag_name" -m "$gc_tag_msg"
        fi
      fi
    fi

    # Clear the scratchpad so it is not accidentally reused
    > "$msg_file"
    echo ""
    echo "$msg_file cleared. Ready for the next commit."
  '';
  publish-wiki = pkgs.writeShellScriptBin "publish-wiki" ''
    set -euo pipefail
    root="$(git rev-parse --show-toplevel)"
    src="$root/wiki"
    # Wiki repo URL: defaults to <origin>.wiki.git; override with arg 1.
    remote="''${1:-$(git -C "$root" remote get-url origin | sed -E 's#\.git$#.wiki.git#')}"

    if [ ! -d "$src" ] || ! ls "$src"/*.md >/dev/null 2>&1; then
      echo "publish-wiki: no wiki pages found in $src" >&2
      exit 1
    fi

    # Pre-flight: every [[link]] must resolve to a page file.
    # (GitHub wikis use Gollum: in [[text|page]] the target is the text AFTER the pipe.)
    echo "Checking wiki link integrity..."
    missing=0
    for target in $({ grep -rhoE '\[\[[^]]+\]\]' "$src"/*.md || true; } \
        | sed -E 's/\[\[//; s/\]\]//; s/^[^|]*\|//; s/ /-/g' | sort -u); do
      if [ ! -f "$src/$target.md" ]; then
        echo "  BROKEN: [[$target]] -> $target.md not found" >&2
        missing=1
      fi
    done
    [ "$missing" -eq 0 ] || { echo "publish-wiki: aborting — fix the broken links above." >&2; exit 1; }
    echo "  links OK ($(ls "$src"/*.md | wc -l) pages)"

    clone="$(mktemp -d)"
    trap 'rm -rf "$clone"' EXIT
    echo "Cloning $remote ..."
    git clone --quiet "$remote" "$clone"
    rm -f "$clone"/*.md
    cp "$src"/*.md "$clone"/
    cd "$clone"
    git add -A
    if git diff --cached --quiet; then
      echo "Wiki already up to date — nothing to publish."
      exit 0
    fi
    echo "Publishing changes:"
    git diff --cached --stat
    git commit -q -m "Sync wiki from wiki/"
    git push
    echo "Published wiki to $remote"
  '';
  # ── OpenGL / X11 runtime for the LWJGL dev client on NixOS ──────────────────
  # `gradle runClient` launches Minecraft's bundled LWJGL 3.3.3, whose GLFW
  # dlopen()s libGLX.so.0 / libGL.so.1 at window-create time. Those glvnd dispatch
  # libs are NOT on the nix devshell's LD_LIBRARY_PATH (which carries only
  # alsa-lib), so boot dies at the last step with
  #   GLFW error 65542: GLX: Failed to load GLX  →  Failed to create window
  # even though every mod loaded fine. Headless `gradle runServer` needs none of
  # this — it never creates a GL context.
  #
  # This list mirrors the game LD_LIBRARY_PATH that the nixpkgs `prismlauncher`
  # wrapper bakes in — the launcher that already runs this exact pack on this
  # machine, so the set is proven-good. ORDER MATTERS: /run/opengl-driver/lib is
  # prepended FIRST (see glLibPath) so the NixOS-managed vendor GL/driver stack
  # resolves ahead of the nix-store client libs. An earlier attempt that appended
  # it LAST + omitted glfw3-minecraft/udev produced a mismatched GL dispatch and
  # hard-crashed the Hyprland/Xwayland display. Keep this in sync with prismlauncher.
  glRuntimeLibs = with pkgs; [
    stdenv.cc.cc.lib      # libstdc++
    glfw3-minecraft       # Minecraft-patched GLFW (what Prism ships)
    openal
    alsa-lib
    libjack2
    libpulseaudio
    pipewire
    libglvnd              # libGLX.so.0 / libGL.so.1 dispatch — the piece missing from the bare devshell
    xorg.libX11
    xorg.libXcursor
    xorg.libXext
    xorg.libXrandr
    xorg.libXxf86vm
    udev                  # libudev (systemd-minimal-libs) — silences the "Did not find udev library" warning
    vulkan-loader
    flite                 # narrator TTS
    gamemode              # feral gamemode client lib
    libusb1
  ];
  glLibPath = "/run/opengl-driver/lib:" + pkgs.lib.makeLibraryPath glRuntimeLibs;
  run-client = pkgs.writeShellScriptBin "run-client" ''
    set -euo pipefail
    root="$(git rev-parse --show-toplevel)"
    cd "$root"
    # NixOS: reproduce PrismLauncher's proven game LD_LIBRARY_PATH (see glRuntimeLibs)
    # so LWJGL's GLFW can create a GL context. This renders on the AMD iGPU / mesa —
    # the compositor's GPU and the known-stable path here. Do NOT force the NVIDIA
    # dGPU (__GLX_VENDOR_LIBRARY_NAME=nvidia): under Hyprland + Xwayland that path has
    # hard-crashed the whole display. If a launch ever misbehaves, contain it in a
    # nested micro-compositor so a GL/driver fault can't take down Hyprland:
    #     gamescope -f -- run-client
    export LD_LIBRARY_PATH="${glLibPath}''${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}"
    # Link any staged world(s) into the dev run dir so you boot on the real map.
    if [ -d mrpack/maps ]; then
      mkdir -p run/saves
      for w in mrpack/maps/*/; do
        [ -d "$w" ] || continue
        name="$(basename "$w")"
        if [ ! -e "run/saves/$name" ]; then
          ln -s "$root/$w" "run/saves/$name" && echo "Linked map: run/saves/$name -> $w"
        fi
      done
    fi
    echo "Launching Fabric dev client (gradle runClient) — run dir: ./run"
    echo "Tip: after editing resources, run 'gradle processResources' + '/cobblemon-initiative reload' in-game (no relaunch)."
    exec gradle runClient "$@"
  '';
  build-mrpack = pkgs.writeShellScriptBin "build-mrpack" ''
    set -euo pipefail
    root="$(git rev-parse --show-toplevel)"
    cd "$root"
    exec ${pkgs.python3}/bin/python3 "$root/scripts/build_mrpack.py" "$@"
  '';
  # Prebuilt uNmINeD CLI (Minecraft map renderer), patched to run on NixOS.
  # The GUI build is Avalonia/X11 and can't render headlessly; this is the CLI.
  # NOTE: this pins a *dev* build by URL + hash. When unmined.net rolls a new dev
  # build the hash will drift and the build will fail printing the new hash — bump
  # `version`, `tmstv`, and `hash` together. (Swap the URL to the non-dev channel
  # for less churn.)
  unmined-cli = let
    version = "0.19.60-dev";
    # Runtime deps for the .NET single-file bundle's extracted native libs
    # (coreclr / globalization / crypto) plus the on-disk rocksdb/deflate libs.
    runtimeLibs = [ pkgs.stdenv.cc.cc.lib pkgs.icu pkgs.openssl pkgs.zlib ];
  in
    pkgs.stdenv.mkDerivation {
      pname = "unmined-cli";
      inherit version;

      src = pkgs.fetchurl {
        url = "https://unmined.net/download/unmined-cli-linux-x64-dev/?tmstv=1781729323";
        name = "unmined-cli_${version}_linux-x64.tar.gz";
        hash = "sha256-XkrjFSyBTS1jJGR+XtKO2z77/c07fkkicniYzeqfiX4=";
      };

      sourceRoot = "unmined-cli_${version}_linux-x64";

      nativeBuildInputs = [ pkgs.autoPatchelfHook pkgs.makeWrapper ];
      # rocksdb-jemalloc (bedrock world codec) pulls these; needed only to satisfy
      # autoPatchelf (resolved via rpath, not the runtime wrapper).
      buildInputs = runtimeLibs ++ [
        pkgs.snappy pkgs.bzip2 pkgs.lz4 pkgs.zstd pkgs.jemalloc
      ];

      dontConfigure = true;
      dontBuild = true;

      installPhase = ''
        runHook preInstall
        mkdir -p "$out/opt/unmined-cli"
        cp -r . "$out/opt/unmined-cli/"
        runHook postInstall
      '';

      postFixup = ''
        makeWrapper "$out/opt/unmined-cli/unmined-cli" "$out/bin/unmined-cli" \
          --prefix LD_LIBRARY_PATH : "${pkgs.lib.makeLibraryPath runtimeLibs}:$out/opt/unmined-cli" \
          --set-default DOTNET_SYSTEM_GLOBALIZATION_INVARIANT 1 \
          --set-default DOTNET_CLI_TELEMETRY_OPTOUT 1
      '';

      meta.description = "uNmINeD command-line Minecraft map renderer (prebuilt, patched for NixOS)";
    };
  zone-mapper = pkgs.writeShellScriptBin "zone-mapper" ''
    set -euo pipefail
    shopt -s nullglob
    root="$(git rev-parse --show-toplevel)"
    src="$root/scripts/zone-mapper"

    port=8099
    open=1
    rerender=0
    world=""
    out=""
    dimension=""
    unmined="''${UNMINED:-unmined-cli}"

    while [ $# -gt 0 ]; do
      case "$1" in
        -w|--world)  world="$2"; shift 2 ;;
        -o|--out)    out="$2"; shift 2 ;;
        -p|--port)   port="$2"; shift 2 ;;
        --dimension) dimension="$2"; shift 2 ;;
        --unmined)   unmined="$2"; shift 2 ;;
        --rerender)  rerender=1; shift ;;
        --no-open)   open=0; shift ;;
        -h|--help)
          echo "Usage: zone-mapper [<world-or-export-dir>] [options]"
          echo ""
          echo "Renders a Minecraft world with uNmINeD into an untracked dir and"
          echo "serves the zone editor over it. With no world given, auto-detects"
          echo "the one staged in mrpack/maps/."
          echo ""
          echo "  -w, --world <dir>   world save to render (must contain level.dat)"
          echo "  -o, --out <dir>     render/serve dir (default: dev/zone-map, untracked)"
          echo "  -p, --port <n>      http port (default: 8099)"
          echo "      --dimension <d> uNmINeD dimension (default: overworld)"
          echo "      --unmined <bin> path to unmined-cli (or the UNMINED env var)"
          echo "      --rerender      re-run uNmINeD even if a render already exists"
          echo "      --no-open       don't open a browser"
          exit 0 ;;
        *)
          if [ -f "$1/unmined.map.properties.js" ]; then out="$1"; else world="$1"; fi
          shift ;;
      esac
    done

    out="''${out:-$root/dev/zone-map}"

    # Auto-detect a staged world (mrpack/maps/<world>/level.dat) if none given.
    if [ -z "$world" ]; then
      for d in "$root"/mrpack/maps/*/; do
        if [ -f "$d/level.dat" ]; then world="''${d%/}"; break; fi
      done
    fi

    if [ ! -f "$out/unmined.map.properties.js" ] || [ "$rerender" -eq 1 ]; then
      if [ -z "$world" ] || [ ! -f "$world/level.dat" ]; then
        echo "No Minecraft world found to render."
        echo "  Looked in: $root/mrpack/maps/*/  (each must contain level.dat)"
        echo "  Or pass one:  zone-mapper --world \"/path/to/save\""
        exit 1
      fi
      if ! command -v "$unmined" >/dev/null 2>&1; then
        echo "uNmINeD CLI not found (tried: $unmined)."
        echo "  Install from https://unmined.net, then put unmined-cli on PATH,"
        echo "  or run:  zone-mapper --unmined /path/to/unmined-cli"
        echo "  (or:     UNMINED=/path/to/unmined-cli zone-mapper)"
        exit 1
      fi
      echo "Rendering with uNmINeD (incremental; first run can take a while)..."
      echo "  world:  $world"
      echo "  output: $out"
      mkdir -p "$out"
      set -- web render --world "$world" --output "$out"
      if [ -n "$dimension" ]; then set -- "$@" --dimension "$dimension"; fi
      "$unmined" "$@"
      echo ""
    fi

    cp "$src/zone-editor.html" "$src/README.md" "$out/"
    [ -d "$src/vendor" ] && cp -r "$src/vendor" "$out/"
    url="http://localhost:$port/zone-editor.html"
    echo "Zone Mapper  ->  $url"
    echo "Serving      $out   (Ctrl-C to stop)"
    if [ "$open" -eq 1 ] && command -v xdg-open >/dev/null 2>&1; then
      ( sleep 1; xdg-open "$url" >/dev/null 2>&1 || true ) &
    fi
    exec ${pkgs.python3}/bin/python3 -m http.server "$port" --directory "$out"
  '';
in
  pkgs.mkShell {
    buildInputs = [
      (inputs.zed-editor.packages.x86_64-linux.default zedSettings)
      pkgs.jdk21
      pkgs.gradle
      pkgs.claude-code
      pkgs.kotlin
      pkgs.python3
      snbt-merge
      gcommit
      publish-wiki
      run-client
      build-mrpack
      unmined-cli
      zone-mapper
    ];

    JAVA_HOME = "${pkgs.jdk21}";
    GRADLE_USER_HOME = ".gradle";


    shellHook = ''
      export PATH="$PWD/scripts:$PATH"

      b=$(printf '\033[1m');   d=$(printf '\033[2m');   r=$(printf '\033[0m')
      cy=$(printf '\033[36m'); ye=$(printf '\033[33m'); gn=$(printf '\033[32m')
      jv=$(java -version 2>&1 | head -n1 | sed -E 's/.*"([^"]+)".*/\1/')
      rule="────────────────────────────────────────────────────────"

      hdr () { printf '  %s%s%s\n'          "$ye$b" "$1" "$r"; }
      cmd () { printf '    %s%-22s%s %s%s%s\n' "$cy" "$1" "$r" "$d" "$2" "$r"; }

      printf '\n  %s%sThe Cobblemon Initiative%s %s— Fabric dev shell%s\n' "$b" "$cy" "$r" "$d" "$r"
      printf   '  %s%s%s\n' "$d" "$rule" "$r"
      printf   '  %sJDK%s %s    %seditor: zeditor .%s\n\n' "$gn" "$r" "$jv" "$d" "$r"

      hdr "build & run"
      cmd "gradle build"          "build the mod → build/libs/"
      cmd "run-client"            "launch the Fabric dev client (live testing)"
      cmd "build-mrpack"          "assemble a .mrpack (--with-map bundles UPM 2)"
      hdr "content"
      cmd "content_compile"       "compile dialog-src/ → Easy NPC SNBT presets"
      cmd "generate_npc_function" "rebuild update_npc_presets.mcfunction"
      cmd "update_preset_index"   "regenerate the Easy NPC preset index"
      cmd "generate_shop_tiers"   "rebuild CobbleDollars shop tiers"
      cmd "snbt-merge --help"     "splice sections between SNBT files"
      hdr "world & map"
      cmd "zone-mapper"           "render the staged map & draw install.json zones"
      hdr "ship it"
      cmd "gcommit"               "commit via GIT_COMMIT_MSG (optional signed tag)"
      cmd "publish-wiki"          "sync wiki/ → the GitHub wiki (checks links)"
      printf '  %s%s%s\n\n' "$d" "$rule" "$r"
    '';
  }
