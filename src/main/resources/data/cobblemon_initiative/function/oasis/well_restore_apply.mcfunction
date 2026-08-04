# Scheduled by well_restore_arm (+2s, chunks force-loaded). Puts back EXACTLY the recorded 44
# water positions (28 sources + 16 re-waterlogged stairs), then a soft splash for anyone standing
# in town. Idempotent — re-setting water over water is harmless.
setblock 2070 125 4048 minecraft:smooth_sandstone_stairs[facing=west,half=bottom,shape=straight,waterlogged=true]
setblock 2070 125 4049 minecraft:water
setblock 2070 125 4050 minecraft:water
setblock 2070 125 4051 minecraft:water
setblock 2070 125 4052 minecraft:smooth_sandstone_stairs[facing=west,half=bottom,shape=straight,waterlogged=true]
setblock 2071 125 4047 minecraft:smooth_sandstone_stairs[facing=north,half=bottom,shape=straight,waterlogged=true]
setblock 2071 125 4048 minecraft:water
setblock 2071 125 4049 minecraft:water
setblock 2071 125 4050 minecraft:water
setblock 2071 125 4051 minecraft:water
setblock 2071 125 4052 minecraft:water
setblock 2071 125 4053 minecraft:smooth_sandstone_stairs[facing=south,half=bottom,shape=straight,waterlogged=true]
setblock 2072 125 4047 minecraft:water
setblock 2072 125 4048 minecraft:water
setblock 2072 125 4049 minecraft:birch_stairs[facing=west,half=bottom,shape=inner_right,waterlogged=true]
setblock 2072 125 4050 minecraft:birch_stairs[facing=west,half=bottom,shape=straight,waterlogged=true]
setblock 2072 125 4051 minecraft:birch_stairs[facing=west,half=bottom,shape=inner_left,waterlogged=true]
setblock 2072 125 4052 minecraft:water
setblock 2072 125 4053 minecraft:water
setblock 2073 125 4047 minecraft:water
setblock 2073 125 4048 minecraft:water
setblock 2073 125 4049 minecraft:birch_stairs[facing=north,half=bottom,shape=straight,waterlogged=true]
setblock 2073 125 4051 minecraft:birch_stairs[facing=south,half=bottom,shape=straight,waterlogged=true]
setblock 2073 125 4052 minecraft:water
setblock 2073 125 4053 minecraft:water
setblock 2074 125 4047 minecraft:water
setblock 2074 125 4048 minecraft:water
setblock 2074 125 4049 minecraft:birch_stairs[facing=north,half=bottom,shape=inner_right,waterlogged=true]
setblock 2074 125 4050 minecraft:birch_stairs[facing=east,half=bottom,shape=straight,waterlogged=true]
setblock 2074 125 4051 minecraft:birch_stairs[facing=south,half=bottom,shape=inner_left,waterlogged=true]
setblock 2074 125 4052 minecraft:water
setblock 2074 125 4053 minecraft:water
setblock 2075 125 4047 minecraft:smooth_sandstone_stairs[facing=north,half=bottom,shape=straight,waterlogged=true]
setblock 2075 125 4048 minecraft:water
setblock 2075 125 4049 minecraft:water
setblock 2075 125 4050 minecraft:water
setblock 2075 125 4051 minecraft:water
setblock 2075 125 4052 minecraft:water
setblock 2075 125 4053 minecraft:smooth_sandstone_stairs[facing=south,half=bottom,shape=straight,waterlogged=true]
setblock 2076 125 4048 minecraft:smooth_sandstone_stairs[facing=east,half=bottom,shape=straight,waterlogged=true]
setblock 2076 125 4049 minecraft:water
setblock 2076 125 4050 minecraft:water
setblock 2076 125 4051 minecraft:water
setblock 2076 125 4052 minecraft:smooth_sandstone_stairs[facing=east,half=bottom,shape=straight,waterlogged=true]
execute positioned 2073.0 126.0 4050.0 run particle minecraft:splash ~ ~ ~ 2.5 0.4 2.5 0.0 120
execute positioned 2073.0 126.0 4050.0 run particle minecraft:falling_water ~ ~1 ~ 2.5 0.6 2.5 0.0 60
execute positioned 2073.0 126.0 4050.0 run playsound minecraft:ambient.underwater.enter ambient @a[distance=..48] 2073.0 126.0 4050.0 0.8 1.1
execute positioned 2073.0 126.0 4050.0 run title @a[distance=..64] actionbar [{"text":"Water murmurs back into the town well.","color":"aqua"}]
forceload remove 2068 4045 2078 4055
