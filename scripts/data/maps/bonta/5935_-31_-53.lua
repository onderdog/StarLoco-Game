local map = MapDef(
	5935,
	"0706131721",
	"715d5a27253242284329714d3d745742593d59653a4347294a47443366415c48382f5223253235795e2171233d305f312248685d394b245167575068715e46615a525b4e54223641682074697c323776313050286e306e56382623736071592144566d552d68483c45772f4068512c62534a40696620645056545740402d4e665d53665c7662",
	"bhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaep1aaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaeaaaaaHhaaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaepZaaaHhH2eaaaaaHhaaep0aaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaepZaaaHhb2eaaaaaHhH2eaaaaaHhaaep0aaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaepZaaaHhH2eaaahUHhb2eaaaaaHhH2eaaaaaHhaaep0aaabhGaeaaaaabhGaeaaaaaHhaaepZaaaHhj2eaadY2HhH2eaaaaaHhr2eqgaaaHhH2eaaaaaHhaaep0aaabhGaeaaaaaHhaaepZaaaHhb2eaaagFHhH2eaaaaaHhH2eaaaaaHhH2eaaaaaHhH2eaaaaaHhaaep0aaabhGaeaaaaaHhb2eaaaaaHhH2eaaaaaHhH2eaaaaaHhH2eaaaaaHhH2eaaaaaHhH2enPaaaHhaaep0aaabhGaeaaaaaHhH2eaaaaaHhH2eaaaaaHhH2eaaaaaHhb2eaaahYHhH2eaaaaaHhH2eaaaaabhGaeaaaaabhGaeaaaaaHhH2eaaaaaHhb2eaaahYHhb2eaaahIHhH2eaaaaaHhH2eaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhH2eaaaaaHhH2eaaaaaHhb2eaaahYHhH2eaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhr2eqgaaaHhH2eaaaaaHhH2eaaaaaHhH2eaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhH2eaaaaaHhH2eaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhH2eaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaa",
	8,
	13,
	-31,
	-53,
	37
)

map.positions = "|"
map.capabilities = 5
map.mobGroupsCount = 3
map.mobGroupsMinSize = 8

-- '0;0;0;0;0;0;0' forbiddens -> capabilities ? Or script ?

map.onMovementEnd = {
	[101] = moveEndTeleport(5936, 79),
	[144] = moveEndTeleport(4291, 430),
}


