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
  run-client = pkgs.writeShellScriptBin "run-client" ''
    set -euo pipefail
    root="$(git rev-parse --show-toplevel)"
    cd "$root"
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
    ];

    JAVA_HOME = "${pkgs.jdk21}";
    GRADLE_USER_HOME = ".gradle";


    shellHook = ''
      export PATH="$PWD/scripts:$PATH"
      echo "Using wrapped local zed: ./.direnv/.config/zed"
      echo "Java version: $(java -version 2>&1 | head -n 1)"
      echo "JAVA_HOME: $JAVA_HOME"
      echo "Use 'zeditor .' for custom editor for repository."
      echo "Use 'gradle build' to build repository. If that fails try removing the '.gradle' directory."
      echo "Use 'run-client' to launch the Fabric dev client (gradle runClient) for live testing."
      echo "Use 'build-mrpack' to assemble a Modrinth .mrpack (add --with-map to bundle the UPM 2 world)."
      echo "Use 'snbt-merge --help' to splice sections between SNBT files."
      echo "Use 'update_preset_index' after adding presets to regenerate the Easy NPC index."
      echo "Use 'generate_npc_function' to rebuild update_npc_presets.mcfunction from npc_presets.json."
      echo "Use 'gcommit' to commit using GIT_COMMIT_MSG (prompts for optional tag)."
      echo "Use 'publish-wiki' to sync wiki/ to the GitHub wiki (defaults to <origin>.wiki.git; checks links first)."
    '';
  }
