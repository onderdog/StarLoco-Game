local map = MapDef(
	6236,
	"0706131721",
	"78364d54672171527c38562863727b3726577444742c64406c544742303e4f4f3b23242d6b3e49404e3a712a5b253235722d723b2532352532353a3548203e3322464d425a2532423a5b43736f584c557d7135245d505f695f282a5a48236776613e6542525c793d5f7e4b69307c5a2d4937437b783a65626872433f5d5b5775413d754d6a46205d61616c5867365c4b3f4e7b4268542629635f6c2e5e60524149433562273f284a3462712d3b2e6a44253242722f403177332532422068525a494e78",
	"bhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaep1aaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaeaaaaaHhaaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaepZaaaHhcjeaaaaaHhaaep0aaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaepZaaaHhIjeoIaaaGhcjeaaaxTHhaaep0aaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaepZaaaGhcjeaaaxSHhIjeaaaaaHhcjeaaap4Hhaaep0aaabhGaeaaaaabhGaeaaaaaHhaaepZaaaHhcjeaaaaaHhIjeaaaaaHhIjeaaaaaHhcjeoIaaaHhaaep0aaabhGaeaaaaaHhaaepZaaaHhsjeqgaaaHhIjeaaaaaHhIjeoJaaaGhcjeaaaxGHhcjeaaaaaHhaaep0aaaHhaaepZaaaHhcjeaaaoiHhIjeaaaaaHhIjeaaaaaHhcjeaaaaaHhcjeaaaaaHhcjeaaap4Hhaaep0aaaGhcjeoJexQHhIjeaaaaaHhIjeaaaaaHhIjeaaaaaHhcjeaaaaaHhIjeh2aaaHhkjeaahY2HhcoeoIaaaHhIjeaaaaaHhIjemWaaaHhIjeaaaaaHhIjeaaaaaHhIjeaaaaaHhIjeaaaaaHhcpeaaaaaHhcoeoIaaaHhIjeoIaaaHhcjeaaahYHhcjeaaahYHhIjeaaaaaHhIjeaaaaaHhcpeaaaaabhGaeaaaaaHhcoeaaaaaHhIjeaaaaaHhIjeaaaaaHhIjeaaaaaHhIjeaaaaaHhcpeaaaaabhGaeaaaaabhGaeaaaaaHhcoeaaaaaHhIjeoIaaaGhcjeaaaxKHhIjeaaaaaHhcpeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhcoeoIaaaHhcjeoJaaaHhIjeaaaaaHhcpeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhcoeaaaaaHhcjeaaahYHhcpeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhcoeoJaaaHhcpeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaaHhaaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaabhGaeaaaaa",
	8,
	13,
	-21,
	34,
	53
)

map.positions = "bRbSb2b5b6cecfcA|bKbYbZb9b-cbclcm"
map.mobGroupsCount = 3
map.mobGroupsMinSize = 8

-- '0;0;0;0;0;0;0' forbiddens -> capabilities ? Or script ?

map.onMovementEnd = {
	[99] = moveEndTeleport(6235, 127),
}


