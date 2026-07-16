# Macro: render the Frontier "seven halls" side objective. Arg: halls (0..6, the count of
# cleared facility brains; the line hides once all seven fall and q.side_frontier_done takes
# over). Mirrors quest/set_wheat.
$scoreboard players display name q.side_frontier_hall ci_quest [{"text":"• Clear the Frontier halls  $(halls)/7","color":"aqua"}]
