local map = MapDef(
	6199,
	"0706131721",
	"413126436f75513a4d5b6169476d375265667d7d214f666e33463f5928642532427036556121726b757c382c6772393a22662f214f253242226c6828586833514d774b673c4c535b4f2a354b2d313d5a567239707a2f5e2532425e67595f45214c4f486f707735783d40337d69484e274e4d513b7e3b3d704d52405f347b552473445c20236b4a44757b714a5e253235253235765575285e747a4d56565e282054376066243174733c6b66522f3e3e2650635c71347a27236d32596d46",
	"bhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaeaaaaaHhaaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaeogaaaHhaaeaaaaaHhaaeogaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaeofaaaHhaAeaaagAHhaaeofeofGhaAeaaaaaHhaaeofiaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaeofaaaHhqAeqgaaaHhaAeaaaqXHhiAeaadTVHhGAeoIaaaHhaaeofiaabhGaeaaaaabhGaeaaaaaHhaaeofaaaHhaAeaaaoiHhGAeoIaaaHhGAeaaaaaHhGAeaaaaaHhaAeaaaaaHhaaeofiaabhGaeaaaaaHhaaeofaaaGhaAeaaaxIHhGAeaaaaaHhGAeaaaaaHhGAeaaaaaHhGAeaaaaaGhaAeaaaxTHhaaeofiaaHhaaeofaaaHhaAeaaaaaHhaAeaaanHHhGAeaaaaaHhGAeaaaaaHhGAeaaaaaHhGAeaaaaaHhaAeaaaaaHhaaeofiaaHhaAeaaaaaHhGAeaaaaaHhGAeaaaaaHhGAeaaaaaHhGAeaaaaaHhGAeaaaaaGhaAeaaaxGHhaAeaaaaaHhaaeaaaaaHhGAeoJaaaHhGAeaaaaaHhGAeaaaaaHhGAeaaaaaHhGAeaaaaaHhaAeaaaaaHhaAeoJaaaHhaaeaaaaaHhaaeaaaaaHhGAeaaaaaHhGAeaaaaaGhaAeaaaxHHhGAeaaaaaHhGAeaaaaaHhaAeaaaaaHhaaeoJaaabhGaeaaaaaHhaaeaaaaaHhGAeaaaaaHhGAeaaaaaGhaAeaaaxHHhGAeaaaaaHhGAeaaaaaHhaaeaaaaabhGaeaaaaabhGaeaaaaaHhaaeaaaaaGhaAeaaaxHHhGAeaaaaaGhaAeaaaxHHhGAeaaaaaHhaaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaeaaaaaGhaAeaaaxHHhGAeaaaaaGhaAeaaaxHHhaaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaeaaaaaGhaAeaaaxHHhiAeoIdY2HhaaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaeaaaaaGhaAeoIaxHHhaaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaeaaaaaHhaaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaa",
	9,
	14,
	-28,
	32,
	53
)

map.positions = "cocBcJcKcScTc2c_|cvcDcEcMcNcVcWc4"
map.mobGroupsCount = 3
map.mobGroupsMinSize = 8

-- '0;0;0;0;0;0;0' forbiddens -> capabilities ? Or script ?

map.onMovementEnd = {
	[113] = moveEndTeleport(6206, 171),
}


