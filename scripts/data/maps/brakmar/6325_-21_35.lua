local map = MapDef(
	6325,
	"0706131721",
	"6A6E262532356872486F58673D6F766254792E427E69514E226E7B3A3D4D43682E342A376E3249587C5E706A534B3D2532356E373C6E27627053465B266E27505B4F7B3C69634B5C3F334547374F2065463321233268362C503A6A69304653607469263B2F23765C503F4C31477B717B33666C2E4533592F5262527047747144672F287638307C4451447E32",
	"bhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaeaaaaaHhaaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeaaaaabhGaeaaaaabhaaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeaaaaabhGaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeaaaaabhGaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeaaaaabhGaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeaaaaabhGaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeofaaaHhaaeofiaaHhaaeaaaaaHhaaeaaaaabhGaeaaaaaHhaaeaaaaaHhaaeaaaaaHhaaeofaaaHhbceaaaoiHhaaeofiaaHhaaeaaaaaHhaaeaaaaabhGaeaaaaaHhaaeaaaxFHhaaeofaaaHhbceaaagBHhrceqgaaaHhaaeofiaaHhaaeaaaaabhGaeaaaaaHhaaeaaaaaHhaaeofaaaGhbceaaaxOHhHceoIaaaHhbceaaaqXHhaaeofiaaHhaaeaaaaabhGaeaaaaaHhaaeofaaaHhbceaaaaaHhHceaaaaaHhHceaaaaaGhbceaaaaaHhaaeofiaabhaaeaaaaaHhaaeofaaaGhbceaaaxOHhHceaaaaaHhHceaaaaaHhbceaaaxGGhbceaaaaaHhaaeofiaabhGaeaaaaaHhbceaaaaaHhHceoIaaaHhHceaaaaaHhbceaaaaaHhbceoIaaaHhbceaaaqXbhGaeaaaaaHhcoeaaaaaHhjceaadY2HhHceaaaaaHhjcerYdTKHhbceaaaaaHhHceaaaaaHhcpeaaaaabhGaeaaaaaHhcoeaaaaaHhHceaaaaaHhHceh2aaaHhHceaaaaaHhHceaaaaaHhcpeaaaaabhGaeaaaaabhGaeaaaaaHhcoeaaaaaHhHceaaaaaHhHceoIaaaHhHceaaaaaHhcpeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhcoeaaaaaHhHceaaaaaHhHceaaaaaHhcpeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhcoeaaaaaHhHceaaaaaHhcpeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhcoeaaaaaHhcpeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaa",
	8,
	12,
	-21,
	35,
	53
)

map.positions = "bwbDbEbKbLbRbSbZ|b2b8b9cdceckclcs"
map.mobGroupsCount = 3
map.mobGroupsMinSize = 8

-- '0;0;0;0;0;0;0' forbiddens -> capabilities ? Or script ?

map.onMovementEnd = {
	[79] = moveEndTeleport(6324, 98),
}


