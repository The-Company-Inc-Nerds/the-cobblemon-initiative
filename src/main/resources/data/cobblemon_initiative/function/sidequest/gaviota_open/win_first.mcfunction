# Gaviota Open (alpha.26 audit rulings) — first win: 600 CD (skew-aware) + the major
# reward pack + the champion latch the port quartet (Enzo/Gianna/Rosa/Marco) already
# gate on. The board-vs-envelope tellraw pair IS the marquee beat: the fiction
# advertises a 5000 CD grand purse (Enzo pitch, Rosa champion entry) and the Company
# verification adjustment settles it to 600 — the skim, played on screen.
tellraw @s [{"text":"The board posts the grand purse: ","color":"gray"},{"text":"5000 CD","color":"gold","bold":true}]
tellraw @s [{"text":"VERIFICATION ADJUSTMENT","color":"red","bold":true},{"text":" — the settled figure in your envelope: ","color":"gray"},{"text":"600 CD","color":"aqua"},{"text":". The Liaison calls this currency stability.","color":"gray"}]
function cobblemon_initiative:economy/payout {amount:600}
function cobblemon_initiative:economy/reward/major
tag @s add gaviota_open_champion
tellraw @s [{"text":"First place in the Gaviota Open. The Weighmaster logs the round; the title, the tide, and a sponsor prize pack are yours.","color":"aqua"}]
