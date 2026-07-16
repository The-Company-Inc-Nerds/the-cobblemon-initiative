# Maintain the frontier_active tag by REGION (run every tick via #minecraft:tick).
# Self-cleaning: the tag is re-derived from position each tick — cleared off everyone, then
# re-added to whoever stands on the Frontier plateau — so it can never leak and wrongly
# disable Nuzlocke elsewhere (the failure mode a set-once tag would risk). NuzlockeInit reads
# this tag to suspend faint damage / party removal / whiteout inside the Frontier: the
# "nothing you love dies on our floor" safe-exhibition promise for the opt-in above-cap grind.
#
# AABB covers the shipped RCT frontier cluster (facilities x~3782-3818, y159, z~2959-2999,
# concourse [3800 159 2997]) with a comfortable margin.
# TODO(showrunner): re-survey this box if the atlas [4096 2965] layout is confirmed (15 Open Q1).
tag @a remove frontier_active
tag @a[x=3760,dx=80,y=150,dy=45,z=2945,dz=70] add frontier_active
