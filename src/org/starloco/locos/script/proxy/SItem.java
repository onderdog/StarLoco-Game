package org.starloco.locos.script.proxy;

import org.classdump.luna.impl.DefaultUserdata;
import org.classdump.luna.impl.ImmutableTable;
import org.classdump.luna.lib.ArgumentIterator;
import org.starloco.locos.client.Player;
import org.starloco.locos.common.SocketManager;
import org.starloco.locos.object.GameObject;
import org.starloco.locos.script.types.MetaTables;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SItem  extends DefaultUserdata<GameObject> {
    private static final ImmutableTable META_TABLE= MetaTables.MetaTable(MetaTables.ReflectIndexTable(SItem.class));

    public SItem(GameObject userValue) {
        super(META_TABLE, userValue);
    }

    @SuppressWarnings("unused")
    private static int guid(GameObject item) {
        return item.getGuid();
    }

    @SuppressWarnings("unused")
    private static int id(GameObject item) {
        return item.getTemplate().getId();
    }

    @SuppressWarnings("unused")
    private static int type(GameObject item) {
        return item.getTemplate().getType();
    }

    @SuppressWarnings("unused")
    private static boolean hasTxtStat(GameObject item, ArgumentIterator args) {
        int stat = args.nextInt();
        String val = args.nextString().toString();

        String stats = item.getTxtStat().get(stat);
        if(stats == null || stats.isEmpty()) {
            return false;
        }
        return Arrays.asList(stats.split(",")).contains(val);
    }

    @SuppressWarnings("unused")
    private static boolean consumeTxtStat(GameObject item, ArgumentIterator args) {
        Player player = args.nextUserdata("SPlayer", SPlayer.class).getUserValue();
        int stat = args.nextInt();
        String val = args.nextString().toString();

        String stats = item.getTxtStat().get(stat);
        if(stats == null || stats.isEmpty())  return false;


        // TODO: change how Text stats are stored. They are a bit hacky currently,
        String newStats = Arrays.stream(stats.split(","))
                .filter(p -> !p.equals(val)) // Filter out val from stat
                .collect(Collectors.joining(","));

        if(newStats.equals(stats)) {
            // New stats are the same as before, we failed to remove the key
            return false;
        }
        item.getTxtStat().put(stat, newStats);

        SocketManager.GAME_SEND_UPDATE_ITEM(player, item);
        return true;
    }

}
