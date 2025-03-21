package org.starloco.locos.client;

import org.starloco.locos.area.Area;
import org.starloco.locos.area.SubArea;
import org.starloco.locos.area.map.Actor;
import org.starloco.locos.area.map.GameCase;
import org.starloco.locos.area.map.GameMap;
import org.starloco.locos.area.map.entity.House;
import org.starloco.locos.area.map.entity.InteractiveObject;
import org.starloco.locos.area.map.entity.MountPark;
import org.starloco.locos.area.map.entity.Trunk;
import org.starloco.locos.client.other.Party;
import org.starloco.locos.client.other.Stalk;
import org.starloco.locos.client.other.Stats;
import org.starloco.locos.command.administration.Group;
import org.starloco.locos.common.Formulas;
import org.starloco.locos.common.SocketManager;
import org.starloco.locos.database.DatabaseManager;
import org.starloco.locos.database.data.game.*;
import org.starloco.locos.database.data.login.AccountData;
import org.starloco.locos.database.data.login.ObjectData;
import org.starloco.locos.database.data.login.PlayerData;
import org.starloco.locos.dynamic.Start;
import org.starloco.locos.entity.Collector;
import org.starloco.locos.entity.Prism;
import org.starloco.locos.entity.mount.Mount;
import org.starloco.locos.entity.pet.Pet;
import org.starloco.locos.entity.pet.PetEntry;
import org.starloco.locos.event.EventManager;
import org.starloco.locos.fight.Fight;
import org.starloco.locos.fight.Fighter;
import org.starloco.locos.fight.spells.Spell;
import org.starloco.locos.fight.spells.SpellEffect;
import org.starloco.locos.game.GameClient;
import org.starloco.locos.game.GameServer;
import org.starloco.locos.game.action.ExchangeAction;
import org.starloco.locos.game.action.GameAction;
import org.starloco.locos.game.action.type.EmptyActionData;
import org.starloco.locos.game.action.type.NpcDialogActionData;
import org.starloco.locos.game.action.type.ScenarioActionData;
import org.starloco.locos.game.world.World;
import org.starloco.locos.guild.GuildMember;
import org.starloco.locos.job.Job;
import org.starloco.locos.job.JobAction;
import org.starloco.locos.job.JobConstant;
import org.starloco.locos.job.JobStat;
import org.starloco.locos.job.maging.BreakingObject;
import org.starloco.locos.kernel.Config;
import org.starloco.locos.kernel.Constant;
import org.starloco.locos.kernel.Main;
import org.starloco.locos.kernel.Reboot;
import org.starloco.locos.lang.LangEnum;
import org.starloco.locos.object.GameObject;
import org.starloco.locos.object.ItemHash;
import org.starloco.locos.object.ObjectSet;
import org.starloco.locos.object.ObjectTemplate;
import org.starloco.locos.other.Action;
import org.starloco.locos.other.Dopeul;
import org.starloco.locos.guild.Guild;
import org.starloco.locos.quest.QuestProgress;
import org.starloco.locos.quest.QuestInfo;
import org.starloco.locos.script.DataScriptVM;
import org.starloco.locos.script.Scripted;
import org.starloco.locos.script.proxy.SPlayer;
import org.starloco.locos.util.Pair;
import org.starloco.locos.util.TimerWaiter;
import org.starloco.locos.database.data.game.SaleOffer.Currency;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.starloco.locos.kernel.Constant.INCARNAM_SUPERAREA;

public class Player implements Scripted<SPlayer>, Actor {
    private static final int MAX_BASIC_JOBS = 3;
    private static final int MIN_JOB_LVL_FOR_NEW_JOB = 30;
    private static final int MIN_JOB_FOR_SPECIALTY = 65;

    private final SPlayer scriptVal;

    public Stats stats;
    //Job
    //Disponibilit�
    public boolean _isAbsent = false;
    public boolean _isInvisible = false;
    //Double
    public boolean _isClone = false;
    //Suiveur - Suivi
    public Map<Integer, Player> follower = new HashMap<>();
    public Player follow = null;
    //Prison Alignement :
    public boolean isInEnnemyFaction;
    public long enteredOnEnnemyFaction;
    public boolean donjon;
    //Commande h�h�
    public int thatMap = -1;
    public int thatCell = -1;
    public boolean walkFast = false;
    public boolean getCases = false;
    public ArrayList<Integer> thisCases = new ArrayList<>();
    public boolean mpToTp = false;
    public boolean noall = false;
    private int id;
    private String name;
    private int sexe;
    private int classe;
    private int color1;
    private int color2;
    private int color3;
    private int level;
    private int energy;
    private long exp;
    private int curPdv;
    private int maxPdv;
    private Stats statsParcho = new Stats(true);
    private long kamas;
    private int _spellPts;
    private int _capital;
    private int _size;
    private int gfxId;
    private int _orientation = 1;
    //PDV
    private int _accID;
    private boolean canAggro = true;
    //Emote
    private List<Integer> emotes = new ArrayList<>();
    //Variables d'ali
    private int alignment = 0;
    private int _deshonor = 0;
    private int _honor = 0;
    private boolean _showWings = false;
    private int _aLvl = 0;
    private GuildMember _guildMember;
    private boolean _showFriendConnection;
    private String _canaux;
    private Fight fight;
    private boolean away;
    private GameMap curMap; // Will become mapInstance GUID
    private GameCase curCell;
    private boolean ready = false;
    private boolean isOnline = false;
    private Party party;
    private int duelId = -1;
    private Map<Integer, SpellEffect> buffs = new HashMap<>();
    private final Map<Integer, GameObject> objects = new HashMap<>();
    private Pair<Integer,Integer> _savePos;
    private int _emoteActive = 0;
    private int savestat;
    private House _curHouse;
    //Invitation
    private int _inviting = 0;
    private ArrayList<Integer> craftingType = new ArrayList<>();
    private Map<Integer, JobStat> _metiers = new HashMap<>();
    //Enclos

    //Monture
    private Mount _mount;
    private int _mountXpGive = 0;
    private boolean _onMount = false;
    //Zaap
    private final ArrayList<Integer> _zaaps = new ArrayList<>();

    //Sort
    private final Map<Integer, Spell.SortStats> _sorts;
    private final Map<Integer, Integer> _sortsPlaces; // K: SpellID, V: Position
    private final Map<Integer, ItemHash> _itemShortcuts = new HashMap<>(); // K: Position, V: Item Hash

    //Titre
    private byte currentTitle = 0;
    //Mariage
    private int wife = 0;
    private int _isOK = 0;
    //Fantome
    private boolean isGhost = false;
    private int _Speed = 0;
    //Marchand
    private boolean _seeSeller = false;
    private Map<Integer, Integer> _storeItems = new HashMap<>();                    //<ObjID, Prix>
    //Metier
    private boolean _metierPublic = false;
    private boolean _livreArti = false;

    //Fight end
    private Fight lastFight;
    private Action endFightAction;
    //Item classe
    private ArrayList<Integer> objectsClass = new ArrayList<>();
    private Map<Integer, World.Couple<Integer, Integer>> objectsClassSpell = new HashMap<>();
    // Taverne
    private long timeTaverne = 0;
    //GA
    private GameAction _gameAction = null;
    //Name
    //Fight :
    private boolean _spec;
    //Traque
    private Stalk _traqued;
    private boolean doAction;
    //FullMorph Stats
    private boolean _morphMode = false;
    private int _morphId;
    private Map<Integer, Spell.SortStats> _saveSorts = new HashMap<>();
    private Map<Integer, Integer> _saveSortsPlaces = new HashMap<>();
    private int _saveSpellPts;
    private int pa = 0,
            pm = 0,
            vitalite = 0,
            sagesse = 0,
            terre = 0,
            feu = 0,
            eau = 0,
            air = 0, initiative = 0;
    private boolean useStats = false;
    private boolean useCac = true;
    // Other ?
    private int oldMap = 0;
    private int oldCell = 0;
    private String _allTitle = "";
    private boolean isBlocked = false;
    //Regen hp
    private boolean sitted;
    private int regenRate = 2000;
    private long regenTime = -1;                                                //-1 veut dire que la personne ne c'est jamais connecte
    private boolean isInPrivateArea = false;
    public Start start;
    private int groupId;
    private boolean isInvisible = false;
    private boolean changeName;
    public boolean afterFight = false;

    private String Savecolors;
    private String Savestats;

    public ArrayList<Integer> getIsCraftingType() {
        return craftingType;
    }

    public Player(int id, String name, int groupe, int sexe, int classe,
                  int color1, int color2, int color3, long kamas, int pts,
                  int _capital, int energy, int level, long exp, int _size,
                  int _gfxid, byte alignement, int account,
                  Map<Integer, Integer> stats, byte seeFriend,
                  byte seeAlign, byte seeSeller, String canaux, short map, int cell,
                  String stuff, String storeObjets, int pdvPer, String spells,
                  String savePos, String jobs, int mountXp, int mount, int honor,
                  int deshonor, int alvl, String zaaps, byte title, int wifeGuid,
                  String morphMode, String allTitle, String emotes, long prison,
                  boolean isNew, String parcho, long timeDeblo, boolean noall, String deadInformation, byte deathCount, long totalKills){
        this(id,
            name,
            groupe,
            sexe,
            classe,
            color1,
            color2,
            color3,
            kamas,
            pts,
            _capital,
            energy,
            level,
            exp,
            _size,
            _gfxid,
            alignement,
            account,
            stats,
            seeFriend,
            seeAlign,
            seeSeller,
            canaux,
            map,
            cell,
            stuff,
            storeObjets,
            pdvPer,
            Collections.emptyMap(),
            Collections.emptyMap(),
            savePos,
            jobs,
            mountXp,
            mount,
            honor,
            deshonor,
            alvl,
            zaaps,
            title,
            wifeGuid,
            morphMode,
            allTitle,
            emotes,
            prison,
            isNew,
            parcho,
            timeDeblo,
            noall,
            deadInformation,
            deathCount,
            totalKills);
        parseSpells(spells, false);
    }

    private Player(int id, String name, int groupe, int sexe, int classe,
                  int color1, int color2, int color3, long kamas, int pts,
                  int _capital, int energy, int level, long exp, int _size,
                  int _gfxid, byte alignement, int account,
                  Map<Integer, Integer> stats, byte seeFriend,
                  byte seeAlign, byte seeSeller, String canaux, short map, int cell,
                  String stuff, String storeObjets, int pdvPer, Map<Integer, Spell.SortStats> spells, Map<Integer,Integer> spellPositions,
                  String savePos, String jobs, int mountXp, int mount, int honor,
                  int deshonor, int alvl, String zaaps, byte title, int wifeGuid,
                  String morphMode, String allTitle, String emotes, long prison,
                  boolean isNew, String parcho, long timeDeblo, boolean noall, String deadInformation, byte deathCount, long totalKills) {
        this.scriptVal = new SPlayer(this);
        this.id = id;
        this.noall = noall;
        this.name = name;
        this.groupId = groupe;
        this.sexe = sexe;
        this.classe = classe;
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
        this.kamas = kamas;
        this._capital = _capital;
        this.alignment = alignement;
        this._honor = honor;
        this._deshonor = deshonor;
        this._aLvl = alvl;
        this.energy = energy;
        this.level = level;
        this.exp = exp;
        if (mount != -1)
            this._mount = World.world.getMountById(mount);
        this._size = _size;
        this.gfxId = _gfxid;
        this._mountXpGive = mountXp;
        this.stats = new Stats(stats, true, this);
        this._accID = account;
        this._showFriendConnection = seeFriend == 1;
        this.wife = wifeGuid;
        this._metierPublic = false;
        this.currentTitle = title;
        this.changeName = false;
        this._allTitle = allTitle;
        this._seeSeller = seeSeller == 1;
        savestat = 0;
        this._canaux = canaux;
        this.curMap = World.world.getMap(map);
        String[] parts = savePos.split(",");
        this._savePos = new Pair<>(Integer.valueOf(parts[0]), Integer.valueOf(parts[1]));
        this.regenTime = System.currentTimeMillis();
        this.timeTaverne = timeDeblo;
        this._sorts = new HashMap<>(spells);
        this._sortsPlaces = new HashMap<>(spellPositions);
        try {
            String[] split = deadInformation.split(",");
            this.dead = Byte.parseByte(split[0]);
            this.deadTime = Long.parseLong(split[1]);
            this.deadType = Byte.parseByte(split[2]);
            this.killByTypeId = Long.parseLong(split[3]);
            if(split.length >= 5)
                this.deadLevel = Short.parseShort(split[4]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.totalKills = totalKills;
        this.deathCount = deathCount;
        try {
            if (!emotes.isEmpty())
                for (String i : emotes.split(";"))
                    this.addStaticEmote(Integer.parseInt(i));
            if (!morphMode.equals("")) {
                if (morphMode.equals("0"))
                    morphMode = "0;0";
                String[] i = morphMode.split(";");
                _morphMode = i[0].equals("1");
                if (!i[1].equals(""))
                    _morphId = Integer.parseInt(i[1]);
            }
            if (_morphMode)
                this._saveSpellPts = pts;
            else
                this._spellPts = pts;
            if (prison != 0) {
                this.isInEnnemyFaction = true;
                this.enteredOnEnnemyFaction = prison;
            }
            this._showWings = this.getAlignment() != 0 && seeAlign == 1;
            if (curMap == null && World.world.getMap(7411) != null) {
                this.curMap = World.world.getMap( 7411);
                this.curCell = curMap.getCase(311);
            } else if (curMap == null && World.world.getMap(7411) == null) {
                throw new IllegalStateException("Cannot find map 7411");
            } else if (curMap != null) {
                this.curCell = curMap.getCase(cell);
                if (curCell == null) {
                    this.curMap = World.world.getMap( 7411);
                    this.curCell = curMap.getCase(311);
                }
            }
            if (!zaaps.equalsIgnoreCase("")) {
                for (String str : zaaps.split(",")) {
                    try {
                        _zaaps.add(Integer.parseInt(str));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!isNew && (curMap == null || curCell == null)) {
                throw new IllegalStateException("Cannot find map/cell for player");
            }
            this.parseObjects(stuff);
            try {
                if (parcho != null && !parcho.equalsIgnoreCase(""))
                    for (String stat : parcho.split(";"))
                        if (!stat.equalsIgnoreCase(""))
                            this.statsParcho.addOneStat(Integer.parseInt(stat.split(",")[0]), Integer.parseInt(stat.split(",")[1]));
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!storeObjets.equals("")) {
                for (String _storeObjets : storeObjets.split("\\|")) {
                    String[] infos = _storeObjets.split(",");
                    int guid = 0;
                    int price = 0;
                    try {
                        guid = Integer.parseInt(infos[0]);
                        price = Integer.parseInt(infos[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }

                    GameObject obj = World.world.getGameObject(guid);
                    if (obj == null)
                        continue;

                    _storeItems.put(obj.getGuid(), price);
                }
            }
            this.maxPdv = (this.level - 1) * 5 + 55
                    + getTotalStats(false).getEffect(Constant.STATS_ADD_VITA)
                    + getTotalStats(false).getEffect(Constant.STATS_ADD_VIE);
            if (this.curPdv <= 0)
                this.curPdv = 1;
            if (pdvPer > 100)
                this.curPdv = (this.maxPdv * 100 / 100);
            else
                this.curPdv = (this.maxPdv * pdvPer / 100);
            if (this.curPdv <= 0)
                this.curPdv = 1;
            //Chargement des m�tiers
            if (!jobs.equals("")) {
                for (String aJobData : jobs.split(";")) {
                    String[] infos = aJobData.split(",");
                    try {
                        int jobID = Integer.parseInt(infos[0]);
                        long xp = Long.parseLong(infos[1]);
                        Job m = World.world.getMetier(jobID);
                        JobStat SM = _metiers.get(learnJob(m));
                        SM.addXp(this, xp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (this.energy == 0)
                setGhost();
            else if (this.energy == -1)
                setFuneral();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseObjects(String stuff) {
        if (!stuff.equals("")) {
            if (stuff.charAt(stuff.length() - 1) == '|')
                stuff = stuff.substring(0, stuff.length() - 1);
            ((ObjectData) DatabaseManager.get(ObjectData.class)).loads(stuff.replace("|", ","));
        }
        for (String item : stuff.split("\\|")) {
            if (item.equals(""))
                continue;
            String[] infos = item.split(":");

            int guid = 0;
            try {
                guid = Integer.parseInt(infos[0]);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            GameObject obj = World.world.getGameObject(guid);
            if (obj != null)
                objects.put(obj.getGuid(), obj);
        }
    }

    //Clone double
    public Player(int id, String name, int groupe, int sexe, int classe,
                  int color1, int color2, int color3, int level, int _size,
                  int _gfxid, Map<Integer, Integer> stats, String stuff,
                  int pdvPer, byte seeAlign, int mount, int alvl, int alignement) {
        this.scriptVal = null; // FIXME if we ever use scripts for fights

        this.id = id;
        this.name = name;
        this.groupId = groupe;
        this.sexe = sexe;
        this.classe = classe;
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
        this.level = level;
        this._aLvl = alvl;
        this._size = _size;
        this.gfxId = _gfxid;
        this.stats = new Stats(stats, true, this);
        this.changeName = false;
        _sorts = new HashMap<>();
        _sortsPlaces = new HashMap<>();
        this.set_isClone(true);

        for (String item : stuff.split("\\|")) {
            if (item.equals(""))
                continue;
            String[] infos = item.split(":");
            int guid = Integer.parseInt(infos[0]);
            GameObject obj = World.world.getGameObject(guid);
            if (obj == null || obj.getPosition() == -1)
                continue;
            GameObject newObj = obj.getClone(obj.getQuantity(), false);
            objects.put(newObj.getGuid(), newObj);
        }
        this.maxPdv = (this.level - 1) * 5 + 50 + getStats().getEffect(Constant.STATS_ADD_VITA);
        this.curPdv = (this.maxPdv * pdvPer) / 100;
        this.alignment = alignement;
        this._showWings = this.getAlignment() != 0 && seeAlign == 1;
        if (mount != -1)
            this._mount = World.world.getMountById(mount);
    }

    public static Player create(String name, int sexe, int classe, int color1, int color2, int color3, Account compte) {
        String z = "";
        if (Config.allZaap) {
            z = Constant.ZAAPS.keySet().stream().map(Object::toString).collect(Collectors.joining(","));
        }
        if (classe > 12 || classe < 1)
            return null;
        if (sexe < 0 || sexe > 1)
            return null;

        int startMapID = Config.startMap > 0 ? (short) Config.startMap : Constant.getStartMap(classe);
        int startCellID = Config.startCell > 0 ? (short) Config.startCell : Constant.getStartCell(classe);

        Player player = new Player(-1, name, -1, sexe, classe, color1, color2, color3, Config.startKamas, ((Config.startLevel - 1)), ((Config.startLevel - 1) * 5), 10000, Config.startLevel,
                World.world.getExperiences().players.minXpAt(Config.startLevel), 100, Integer.parseInt(classe
                + "" + sexe), (byte) 0, compte.getId(), new HashMap<>(), (byte) 1, (byte) 0, (byte) 0, "*#%!pi$:?",
                (short)startMapID,
                startCellID,
                "", "", 100, Constant.getStartSorts(classe), Constant.getStartSortsPlaces(classe),
                String.format("%d,%d", startMapID, startCellID),
                "", 0, -1, 0, 0, 0, z, (byte) 0, 0, "0;0", "", (Config.allEmotes ? "0;1;2;3;4;5;6;7;8;9;10;11;12;13;14;15;16;17;18;19;20;21" : "0"), 0, true, "118,0;119,0;123,0;124,0;125,0;126,0", 0, false, "0,0,0,0", (byte) 0, 0);

        player.emotes.add(0);
        player.emotes.add(1);
        for (int a = 1; a <= player.getLevel(); a++)
            Constant.onLevelUpSpells(player, a);

        if (!((PlayerData) DatabaseManager.get(PlayerData.class)).insert(player))
            return null;

        SocketManager.GAME_SEND_WELCOME(player);
        World.world.sendMessageToAll("client.player.onjoingame.welcome", player.getName());

        World.world.addPlayer(player);

        return player;
    }

    public static String getCompiledEmote(List<Integer> i) {
        int i2 = 0;
        for (Integer b : i) i2 += (2 << (b - 2));
        return i2 + "|0";
    }

    //CLONAGE
    public static Player ClonePerso(Player P, int id, int pdv) {
        HashMap<Integer, Integer> stats = new HashMap<>();
        stats.put(Constant.STATS_ADD_VITA, pdv);
        stats.put(Constant.STATS_ADD_FORC, P.getStats().getEffect(Constant.STATS_ADD_FORC));
        stats.put(Constant.STATS_ADD_SAGE, P.getStats().getEffect(Constant.STATS_ADD_SAGE));
        stats.put(Constant.STATS_ADD_INTE, P.getStats().getEffect(Constant.STATS_ADD_INTE));
        stats.put(Constant.STATS_ADD_CHAN, P.getStats().getEffect(Constant.STATS_ADD_CHAN));
        stats.put(Constant.STATS_ADD_AGIL, P.getStats().getEffect(Constant.STATS_ADD_AGIL));
        stats.put(Constant.STATS_ADD_PA, P.getStats().getEffect(Constant.STATS_ADD_PA));
        stats.put(Constant.STATS_ADD_PM, P.getStats().getEffect(Constant.STATS_ADD_PM));
        stats.put(Constant.STATS_ADD_RP_NEU, P.getStats().getEffect(Constant.STATS_ADD_RP_NEU));
        stats.put(Constant.STATS_ADD_RP_TER, P.getStats().getEffect(Constant.STATS_ADD_RP_TER));
        stats.put(Constant.STATS_ADD_RP_FEU, P.getStats().getEffect(Constant.STATS_ADD_RP_FEU));
        stats.put(Constant.STATS_ADD_RP_EAU, P.getStats().getEffect(Constant.STATS_ADD_RP_EAU));
        stats.put(Constant.STATS_ADD_RP_AIR, P.getStats().getEffect(Constant.STATS_ADD_RP_AIR));
        stats.put(Constant.STATS_ADD_AFLEE, P.getStats().getEffect(Constant.STATS_ADD_AFLEE));
        stats.put(Constant.STATS_ADD_MFLEE, P.getStats().getEffect(Constant.STATS_ADD_MFLEE));

        byte showWings = 0;
        int alvl = 0;
        if (P.getAlignment() != 0 && P._showWings) {
            showWings = 1;
            alvl = P.getGrade();
        }
        int mountID = -1;
        if (P.getMount() != null) {
            mountID = P.getMount().getId();
        }

        Player Clone = new Player(id, P.getName(), (P.getGroup() != null) ? P.getGroup().getId() : -1, P.getSexe(), P.getClasse(), P.getColor1(), P.getColor2(), P.getColor3(), P.getLevel(), 100, P.getGfxId(), stats, "", 100, showWings, mountID, alvl, P.getAlignment());
        Clone.objects.putAll(P.objects);
        Clone.set_isClone(true);
        if (P._onMount) {
            Clone._onMount = true;
        }
        return Clone;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
        this.changeName = false;

        ((PlayerData) DatabaseManager.get(PlayerData.class)).updateInfos(this);
        if (this.getGuildMember() != null)
            ((GuildMemberData) DatabaseManager.get(GuildMemberData.class)).update(this);
    }


    public void setColors(int i, int i1, int i2) {
        this.color1 = i;
        this.color2 = i1;
        this.color3 = i2;
        ((PlayerData) DatabaseManager.get(PlayerData.class)).updateInfos(this);
    }

    public Group getGroup() {
        return Group.byId(this.groupId);
    }

    public void setGroupe(int groupId, boolean reload) {
        this.groupId = groupId;
        if (reload)
            ((PlayerData) DatabaseManager.get(PlayerData.class)).updateGroupe(this);
    }

    public boolean isInvisible() {
        return this.isInvisible;
    }

    public void setInvisible(boolean b) {
        this.isInvisible = b;
    }

    public int getSexe() {
        return this.sexe;
    }

    public void setSexe(int sexe) {
        this.sexe = sexe;
        this.setGfxId(10 * this.getClasse() + this.sexe);
    }

    public int getClasse() {
        return this.classe;
    }

    public void setClasse(int classe) {
        this.classe = classe;
    }

    public int getColor1() {
        return this.color1;
    }

    public int getColor2() {
        return this.color2;
    }

    public int getColor3() {
        return this.color3;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getEnergy() {
        return this.energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public long getExp() {
        return this.exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public int getCurPdv() {
        refreshLife(false);
        return this.curPdv;
    }

    public void setPdv(int pdv) {
        this.curPdv = pdv;
        if (this.curPdv >= this.maxPdv)
            this.curPdv = this.maxPdv;
        if (party != null)
            SocketManager.GAME_SEND_PM_MOD_PACKET_TO_GROUP(party, this);
    }

    public int getMaxPdv() {
        return this.maxPdv;
    }

    public void setMaxPdv(int maxPdv) {
        this.maxPdv = maxPdv;
        SocketManager.GAME_SEND_STATS_PACKET(this);
        if (party != null)
            SocketManager.GAME_SEND_PM_MOD_PACKET_TO_GROUP(party, this);
    }

    public Stats getStats() {
        if (useStats)
            return newStatsMorph();
        else
            return this.stats;
    }

    public Stats getStatsParcho() {
        return statsParcho;
    }

    public String parseStatsParcho() {
        String parcho = "";
        for (Entry<Integer, Integer> i : statsParcho.getEffects().entrySet())
            parcho += (parcho.isEmpty() ? i.getKey() + "," + i.getValue() : ";" + i.getKey() + "," + i.getValue());
        return parcho;
    }

    public boolean getDoAction() {
        return doAction;
    }

    public void setDoAction(boolean b) {
        doAction = b;
    }

    public void setRoleplayBuff(int id) {
        int objTemplate = 0;
        switch (id) {
            case 10673:
                objTemplate = 10844;
                break;
            case 10669:
                objTemplate = 10681;
                break;
        }
        if (objTemplate == 0)
            return;
        if (getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF) != null) {
            int guid = getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF).getGuid();
            SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
            this.deleteItem(guid);
        }

        GameObject obj = World.world.getObjTemplate(objTemplate).createNewRoleplayBuff();
        this.addItem(obj, false, false);
        World.world.addGameObject(obj);
        SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
        SocketManager.GAME_SEND_Ow_PACKET(this);
        SocketManager.GAME_SEND_STATS_PACKET(this);
        ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
    }

    public void setBenediction(int id) {
        if (getObjetByPos(Constant.ITEM_POS_BENEDICTION) != null) {
            int guid = getObjetByPos(Constant.ITEM_POS_BENEDICTION).getGuid();
            SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
            this.deleteItem(guid);
        }
        if (id == 0) {
            SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
            return;
        }
        int turn = 0;
        switch (id) {
            case 10682:
                turn = 20;
                break;
            default:
                turn = 1;
                break;
        }

        GameObject obj = World.world.getObjTemplate(id).createNewBenediction(turn);
        this.addItem(obj, false, false);
        World.world.addGameObject(obj);
        SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
        SocketManager.GAME_SEND_Ow_PACKET(this);
        SocketManager.GAME_SEND_STATS_PACKET(this);
        ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
    }

    public void setMalediction(int id) {
        int objTemplate = 0;
        switch (id) {
            case 10827:
                objTemplate = 10838;
                break;
            default:
                objTemplate = id;
        }
        if (objTemplate == 0) {
            SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
            return;
        }
        if (getObjetByPos(Constant.ITEM_POS_MALEDICTION) != null) {
            int guid = getObjetByPos(Constant.ITEM_POS_MALEDICTION).getGuid();
            SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
            this.deleteItem(guid);
        }

        GameObject obj = World.world.getObjTemplate(objTemplate).createNewMalediction();
        this.addItem(obj, false, false);
        World.world.addGameObject(obj);
        if (this.getFight() != null) {
            SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
            SocketManager.GAME_SEND_Ow_PACKET(this);
            SocketManager.GAME_SEND_STATS_PACKET(this);
            ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
        }
    }

    public void setMascotte(int id) {
        if (getObjetByPos(Constant.ITEM_POS_PNJ_SUIVEUR) != null) {
            int guid = getObjetByPos(Constant.ITEM_POS_PNJ_SUIVEUR).getGuid();
            SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
            this.deleteItem(guid);
        }
        if (id == 0) {
            SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
            return;
        }

        GameObject obj = World.world.getObjTemplate(id).createNewFollowPnj(1);
        if (obj != null)
            if (this.addItem(obj, false, false))
                World.world.addGameObject(obj);

        SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
        SocketManager.GAME_SEND_Ow_PACKET(this);
        SocketManager.GAME_SEND_STATS_PACKET(this);
        ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
    }

    public void setCandy(int id) {
        if (getObjetByPos(Constant.ITEM_POS_BONBON) != null) {
            int guid = getObjetByPos(Constant.ITEM_POS_BONBON).getGuid();
            SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
            this.deleteItem(guid);
        }
        int turn = 30;
        switch (id) {
            case 8948:
            case 8949:
            case 8950:
            case 8951:
            case 8952:
            case 8953:
            case 8954:
            case 8955:
                turn = 5;
                break;
            case 10665:
                turn = 20;
                break;
            default:
                turn = 30;
                break;
        }

        GameObject obj = World.world.getObjTemplate(id).createNewCandy(turn);
        this.addItem(obj, false, false);
        World.world.addGameObject(obj);
        SocketManager.GAME_SEND_Ow_PACKET(this);
        SocketManager.GAME_SEND_STATS_PACKET(this);
        ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
    }

    public void calculTurnCandy() {
        GameObject obj = getObjetByPos(Constant.ITEM_POS_BONBON);
        if (obj != null) {
            obj.getStats().addOneStat(Constant.STATS_TURN, -1);
            if (obj.getStats().getEffect(Constant.STATS_TURN) <= 0) {
                SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, obj.getGuid());
                this.deleteItem(obj.getGuid());
            } else {
                SocketManager.GAME_SEND_UPDATE_ITEM(this, obj);
            }
            ((ObjectData) DatabaseManager.get(ObjectData.class)).update(obj);
        }
        obj = getObjetByPos(Constant.ITEM_POS_PNJ_SUIVEUR);
        if (obj != null) {
            obj.getStats().addOneStat(Constant.STATS_TURN, -1);
            if (obj.getStats().getEffect(Constant.STATS_TURN) <= 0) {
                SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, obj.getGuid());
                this.deleteItem(obj.getGuid());
            } else {
                SocketManager.GAME_SEND_UPDATE_ITEM(this, obj);
            }
            ((ObjectData) DatabaseManager.get(ObjectData.class)).update(obj);
        }
        obj = getObjetByPos(Constant.ITEM_POS_BENEDICTION);
        if (obj != null) {
            obj.getStats().addOneStat(Constant.STATS_TURN, -1);
            if (obj.getStats().getEffect(Constant.STATS_TURN) <= 0) {
                SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, obj.getGuid());
                this.deleteItem(obj.getGuid());
            } else {
                SocketManager.GAME_SEND_UPDATE_ITEM(this, obj);
            }
            ((ObjectData) DatabaseManager.get(ObjectData.class)).update(obj);
        }
        obj = getObjetByPos(Constant.ITEM_POS_MALEDICTION);
        if (obj != null) {
            obj.getStats().addOneStat(Constant.STATS_TURN, -1);
            if (obj.getStats().getEffect(Constant.STATS_TURN) <= 0) {
                gfxId = getClasse() * 10 + getSexe();
                if (this.getFight() == null)
                    SocketManager.GAME_SEND_ALTER_GM_PACKET(getCurMap(), this);
                SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, obj.getGuid());
                switch (obj.getTemplate().getId()) {
                    case 8169:
                    case 8170:
                        unsetFullMorph();
                        break;
                }

                this.deleteItem(obj.getGuid());
            } else {
                SocketManager.GAME_SEND_UPDATE_ITEM(this, obj);
            }
            ((ObjectData) DatabaseManager.get(ObjectData.class)).update(obj);
        }
        obj = getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF);
        if (obj != null) {
            obj.getStats().addOneStat(Constant.STATS_TURN, -1);
            if (obj.getStats().getEffect(Constant.STATS_TURN) <= 0) {
                gfxId = getClasse() * 10 + getSexe();
                SocketManager.GAME_SEND_ALTER_GM_PACKET(getCurMap(), this);
                SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, obj.getGuid());
                this.deleteItem(obj.getGuid());
            } else {
                SocketManager.GAME_SEND_UPDATE_ITEM(this, obj);
            }
            ((ObjectData) DatabaseManager.get(ObjectData.class)).update(obj);
        }
    }

    public List<Spell.SortStats> getSpells() {
        return new ArrayList<>(_sorts.values());
    }

    public boolean isSpec() {
        return _spec;
    }

    public void setSpec(boolean s) {
        this._spec = s;
    }

    public String getAllTitle() {
        _allTitle = ((PlayerData) DatabaseManager.get(PlayerData.class)).loadTitles(this.getId());
        return _allTitle;
    }

    public void setAllTitle(String title) {
        getAllTitle();
        boolean erreur = false;
        if (title.equals(""))
            title = "0";
        if (_allTitle != null)
            for (String i : _allTitle.split(","))
                if (i.equals(title))
                    erreur = true;
        if (_allTitle == null && !erreur)
            _allTitle = title;
        else if (!erreur)
            _allTitle += "," + title;
        ((PlayerData) DatabaseManager.get(PlayerData.class)).updateTitles(this.getId(), _allTitle);
    }

    public void teleportOldMap() {
        if(oldMap > 0) {
            this.teleport(oldMap, oldCell);
        }
        this.oldMap = -1;
        this.oldCell = -1;
    }

    public void setCurrentPositionToOldPosition() {
        if(this.oldMap != -1) {
            this.curMap = World.world.getMap(this.oldMap);
            this.curCell = this.curMap.getCase(this.oldCell);
        }
    }

    public void setOldPosition() {
        this.oldMap = this.getCurMap().getId();
        this.oldCell = this.getCurCell().getId();
    }

    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
    }

    public String encodeSpellsToDB() {
        StringBuilder sorts = new StringBuilder();

        Map<Integer, Spell.SortStats> spells = _sorts;
        Map<Integer, Integer> positions = _sortsPlaces;

        if (_morphMode) {
            spells = _saveSorts;
            positions = _saveSortsPlaces;
        }

        if (spells.isEmpty())
            return "";
        for (int key : spells.keySet()) {
            Spell.SortStats SS = spells.get(key);
            if (SS == null)
                continue;
            sorts.append(SS.getSpellID()).append(";").append(SS.getLevel()).append(";");
            int position = positions.getOrDefault(key, 126);
            if (position > 0 && position < 31)
                sorts.append(position);
            sorts.append(",");
        }
        return sorts.substring(0, sorts.length() - 1);
    }

    public void parseSpells(String str, boolean send) {
        if(str.length() == 0) throw new IllegalArgumentException("passed empty spell string to parseSpells");
//        if(str.equalsIgnoreCase("")) {
//            _sorts = Constant.getStartSorts(classe);
//            for (int a = 1; a <= this.getLevel(); a++)
//                Constant.onLevelUpSpells(this, a);
//            this._sortsPlaces = Constant.getStartSortsPlaces(this.classe);
//            return;
//        }

        Map<Integer, Spell.SortStats> spells = _sorts;
        Map<Integer, Integer> spellPositions = _sortsPlaces;
        if (_morphMode) {
            spells = _saveSorts;
            spellPositions = _saveSortsPlaces;
        }
        spells.clear();
        spellPositions.clear();

        String[] spellParts = str.split(",");
        for (String e : spellParts) {
            try {
                String[] parts = e.split(";");
                int id = Integer.parseInt(parts[0]);
                int lvl = Integer.parseInt(parts[1]);

                Spell.SortStats ss =  World.world.getSort(id).getStatsByLevel(lvl);
                if(ss == null) throw new IllegalStateException(String.format("player has unknown spell: %d/%d", id, lvl));
                spells.put(id, World.world.getSort(id).getStatsByLevel(lvl));

                if(parts.length < 3 || parts[2].equalsIgnoreCase("")) continue;
                int position = World.world.getCryptManager().getIntByHashedValue(parts[2].charAt(0)); // may return -1
                if(position == 63) continue; // It was "_" which means no shortcut
                if(position > 30) {
                    // Too high to be a valid base64 position
                    position =  Integer.parseInt(parts[2]);
                }
                spellPositions.put(id, position);
            } catch (NumberFormatException e1) {
                Main.logger.error("Cannot load player's spell", e1);
            }
        }
    }

    private void parseSpellsFullMorph(String str) {
        String[] spells = str.split(",");
        _sorts.clear();
        _sortsPlaces.clear();
        for (String e : spells) {
            try {
                String[] parts = e.split(";");
                int id = Integer.parseInt(parts[0]);
                int lvl = Integer.parseInt(parts[1]);
                int position = World.world.getCryptManager().getIntByHashedValue(parts[2].charAt(0)); // May return -1
                if(position == 63) continue; // base64 '_' -> decimal 63: Placeholder for "No shortcut"

                // Are we using the new 1.39 way: (Only decimals)
                if(parts[2].length() > 1 || position > 30) {
                    position =  Integer.parseInt(parts[2]);
                }

                if (!_morphMode)
                    learnSpell(id, lvl, false, false, false);
                else
                    learnSpell(id, lvl, false, true, false);
                _sortsPlaces.put(id, position);
            } catch (NumberFormatException e1) {
                e1.printStackTrace();
            }
        }
    }

    public Pair<Integer, Integer> getSavePosition() {
        return _savePos;
    }

    public void setSavePos(int mapID, int cellID) {
        _savePos = new Pair<>(mapID, cellID);
    }

    public long getKamas() {
        return kamas;
    }

    public void setKamas(long l) {
        this.kamas = l;
    }

    public Map<Integer, SpellEffect> get_buff() {
        return buffs;
    }

    public Account getAccount() {
        return World.world.ensureAccountLoaded(_accID);
    }

    public int get_spellPts() {
        if (_morphMode)
            return _saveSpellPts;
        else
            return _spellPts;
    }

    public void setSpellPoints(int pts) {
        if (_morphMode)
            _saveSpellPts = pts;
        else
            _spellPts = pts;
    }

    public Guild getGuild() {
        if (_guildMember == null)
            return null;
        return _guildMember.getGuild();
    }

    public void setChangeName(boolean changeName) {
        this.changeName = changeName;
        if (changeName) this.send("AlEr");
    }

    public boolean isChangeName() {
        return changeName;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public int getDuelId() {
        return duelId;
    }

    public void setDuelId(int _duelid) {
        duelId = _duelid;
    }

    public Fight getFight() {
        return fight;
    }

    public void setFight(Fight fight) {
        if(this.setSitted(false) || fight != null) {
            regenRate = 0;
            this.send("ILF0");
        } else if(fight == null) {
            regenRate = 1000;
            this.send("ILS1000");
        }
        this.fight = fight;
    }

    public boolean is_showFriendConnection() {
        return _showFriendConnection;
    }

    public boolean is_showWings() {
        return _showWings;
    }

    public boolean isShowSeller() {
        return _seeSeller;
    }

    public void setShowSeller(boolean is) {
        _seeSeller = is;
    }

    public String get_canaux() {
        return _canaux;
    }

    public GameCase getCurCell() {
        return curCell;
    }

    public void setCurCell(GameCase cell) {
        curCell = cell;
    }

    public int get_size() {
        return _size;
    }

    public void set_size(int _size) {
        this._size = _size;
    }

    public int getGfxId() {
        return gfxId;
    }

    public void setGfxId(int _gfxid) {
        if (this.getClasse() * 10 + this.getSexe() != _gfxid) {
            if (this.isOnMount())
                this.toogleOnMount();
            this.send(_gfxid != 8004 ?"AR3K" : "AR6bK");
        } else {
            this.send("AR6bK");
        }
        gfxId = _gfxid;
    }

    public boolean isMorphMercenaire() {
        return (this.gfxId == 8009 || this.gfxId == 8006);
    }

    public GameMap getCurMap() {
        return curMap;
    }

    public void setCurMap(GameMap curMap) {
        this.curMap = curMap;
    }

    public boolean isAway() {
        return away;
    }

    public void setAway(boolean away) {
        this.away = away;
    }

    public boolean isSitted() {
        return sitted;
    }

    public boolean setSitted(boolean sitted) {
        if (this.sitted != sitted) {
            this.refreshLife(this.sitted);
            this.sitted = sitted;
            this.regenRate = (sitted ? 1000 : 2000);
            SocketManager.send(this, "ILS" + regenRate);
            return true;
        }
        return false;
    }

    public int getCapital() {
        return _capital;
    }

    public boolean canLearnJob(int jobID, boolean sendIm) {
        Job job = World.world.getMetier(jobID);
        if(job == null) return false;


        if(getMetierByID(jobID) != null) {
            // Already known
            if(sendIm) {
                SocketManager.GAME_SEND_Im_PACKET(this, "111");
            }
            return false;
        }

        if(totalJobBasic()>=MAX_BASIC_JOBS) {
            if(sendIm) {
                SocketManager.GAME_SEND_Im_PACKET(this, "19");
            }
            return false;
        }

        // Common Precondition: All current jobs > 30
        int min = _metiers.values().stream().mapToInt(JobStat::get_lvl).min().orElse(100);// 100 is the max level, so that's sure to be enough
        if(min < MIN_JOB_LVL_FOR_NEW_JOB) return false;

        if(job.isMaging()) {
            // Magus Precondition: Less than 3 magus jobs
            if (totalJobFM() > 2) {
                if(sendIm) {
                    SocketManager.GAME_SEND_Im_PACKET(this, "19");
                }
                return false;
            }

            JobStat baseJobStats = _metiers.get(World.world.getMetierByMaging(jobID));
            if(baseJobStats == null || baseJobStats.get_lvl() < MIN_JOB_FOR_SPECIALTY) {
                if(sendIm) {
                    SocketManager.GAME_SEND_Im_PACKET(this, "111");
                }
                return false;
            }
        }

        return true;
    }

    public boolean tryLearnJob(int jobID) {
        Job job = World.world.getMetier(jobID);
        if(job == null) return false;

        if(!canLearnJob(jobID, true)) return false;
        learnJob(job);
        return true;
    }

    public void startScenario(int id, String date, BiConsumer<Player,Boolean> onEnd) {
        exchangeAction =  new ExchangeAction<>(
                ExchangeAction.IN_SCENARIO,
                new ScenarioActionData(exchangeAction, onEnd));
        SocketManager.GAME_SEND_TUTORIAL_CREATE(this, id, date);
    }

    @Override
    public long Id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    public void openDocument(int id, String date) {
        exchangeAction =  new ExchangeAction<>(ExchangeAction.USING_OBJECT, EmptyActionData.INSTANCE);
        SocketManager.GAME_SEND_DOCUMENT_CREATE_PACKET(getGameClient(), id, date);
    }

    public void showReceivedItem(int actorID, int quantity) {
        SocketManager.GAME_SEND_IQ_PACKET(this, actorID, quantity);
    }


    public static class EnsureSpellLevelResult {
        public final boolean changed;
        public final int ptsDelta, oldLevel;
        public final boolean worked; // can only be false when spell/level doesn't exist, or modPoints is true

        public EnsureSpellLevelResult(boolean changed, int ptsDelta, int oldLevel, boolean worked) {
            this.changed = changed;
            this.ptsDelta = ptsDelta;
            this.oldLevel = oldLevel;
            this.worked = worked;
        }
    }

    // returns Couple<ptsDelta,worked> Worked can only be false when spell/level doesn't exist, or modPoints is true.
    public EnsureSpellLevelResult ensureSpellLevelSilent(int spell, int newLevel, boolean modPoints) {
        int previousLevel = Optional.ofNullable(_sorts.get(spell)).map(Spell.SortStats::getLevel).orElse(0);

        // Already in the state we want
        if(previousLevel==newLevel) return new EnsureSpellLevelResult(false, 0, 0, true);

        Spell.SortStats ss = Optional.ofNullable(World.world.getSort(spell)).map(s -> s.getStatsByLevel(newLevel)).orElse(null);
        if(ss==null) return new EnsureSpellLevelResult(false, 0, 0, false);

        int ptsDelta = 0;
        if(modPoints) {
            // Compute price changing from lvl 1 -> N:  Price(N) = SumInt(N-1) with SumInt(n) = n(n+1)/2
            // modPoints(Old,New) =  Price(Old) - Price(New)
            ptsDelta = (previousLevel*(previousLevel-1) - (newLevel * (newLevel-1)))/2;

            if(_spellPts < ptsDelta) {
                // Not enough points
                return new EnsureSpellLevelResult(false, 0, previousLevel, false);
            }
            _spellPts += ptsDelta;
        }

        // Set spell
        _sorts.put(spell, ss);
        return new EnsureSpellLevelResult(true, ptsDelta, previousLevel, true);
    }

    public boolean ensureSpellLevel(int spell, int level, boolean modPoints, boolean silent) {
        EnsureSpellLevelResult result = ensureSpellLevelSilent(spell, level, modPoints);

        if(!result.worked) return false;
        if(silent || !isOnline || !result.changed) return true;

        SocketManager.GAME_SEND_SPELL_LIST(this);
        if(result.oldLevel == 0) {
            // Learned // Do we need a different message to show  c
            SocketManager.GAME_SEND_Im_PACKET(this, "03;" + spell);
        } else if (level == 0){
            // Unlearned // Do we need a different message ID for negative deltas ?
            SocketManager.GAME_SEND_Im_PACKET(this, "0154;" + "<b>" + result.oldLevel + "</b>" + "~" + "<b>" + result.ptsDelta + "</b>");
        } else {
            // Change level
            SocketManager.GAME_SEND_SPELL_UPGRADE_SUCCESS(this.getGameClient(), id, level);

        }
        if(result.ptsDelta!=0) {
            SocketManager.GAME_SEND_STATS_PACKET(this);
        }
        return true;
    }

    public void learnSpell(int spell, int level, int pos) {
        if (World.world.getSort(spell).getStatsByLevel(level) == null) {
            GameServer.a();
            return;
        }

        if (!_sorts.containsKey(spell)) {
            _sorts.put(spell, World.world.getSort(spell).getStatsByLevel(level));
            removeSpellShortcutAtPosition(pos);
            _sortsPlaces.remove(spell);
            _sortsPlaces.put(spell, pos);
            SocketManager.GAME_SEND_SPELL_LIST(this);
            SocketManager.GAME_SEND_Im_PACKET(this, "03;" + spell);
        }
    }

    public boolean learnSpell(int spellID, int level, boolean save, boolean send, boolean learn) {
        if (World.world.getSort(spellID).getStatsByLevel(level) == null) {
            GameServer.a();
            return false;
        }

        if (_sorts.containsKey(spellID) && learn) {
            SocketManager.GAME_SEND_MESSAGE(this, this.getLang().trans("client.player.learnspell.exist"));
            return false;
        } else {
            _sorts.put(spellID, World.world.getSort(spellID).getStatsByLevel(level));
            if (send) {
                SocketManager.GAME_SEND_SPELL_LIST(this);
                SocketManager.GAME_SEND_Im_PACKET(this, "03;" + spellID);
            }
            if (save)
                ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
            return true;
        }
    }

    public boolean unlearnSpell(int spell) {
        if (World.world.getSort(spell) == null) {
            GameServer.a();
            return false;
        }

        _sorts.remove(spell);
        this._sortsPlaces.remove(spell);
        SocketManager.GAME_SEND_SPELL_LIST(this);
        SocketManager.GAME_SEND_STATS_PACKET(this);
        ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
        return true;
    }

    public boolean unlearnSpell(Player perso, int spellID, int level,
                                int ancLevel, boolean save, boolean send) {
        int spellPoint = 1;
        if (ancLevel == 2)
            spellPoint = 1;
        if (ancLevel == 3)
            spellPoint = 2 + 1;
        if (ancLevel == 4)
            spellPoint = 3 + 3;
        if (ancLevel == 5)
            spellPoint = 4 + 6;
        if (ancLevel == 6)
            spellPoint = 5 + 10;

        if (World.world.getSort(spellID).getStatsByLevel(level) == null) {
            GameServer.a();
            return false;
        }

        _sorts.put(Integer.valueOf(spellID), World.world.getSort(spellID).getStatsByLevel(level));
        if (send) {
            SocketManager.GAME_SEND_SPELL_LIST(this);
            SocketManager.GAME_SEND_Im_PACKET(this, "0154;" + "<b>" + ancLevel
                    + "</b>" + "~" + "<b>" + spellPoint + "</b>");
            addSpellPoint(spellPoint);
            SocketManager.GAME_SEND_STATS_PACKET(perso);
        }
        if (save)
            ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
        return true;
    }

    public boolean boostSpell(int spellID) {
        if (getSortStatBySortIfHas(spellID) == null)
            return false;
        int AncLevel = getSortStatBySortIfHas(spellID).getLevel();
        if (AncLevel == 6)
            return false;
        if (_spellPts >= AncLevel && World.world.getSort(spellID).getStatsByLevel(AncLevel + 1).getReqLevel() <= this.getLevel()) {
            if (learnSpell(spellID, AncLevel + 1, true, false, false)) {
                _spellPts -= AncLevel;
                ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
                return true;
            } else {
                return false;
            }
        } else
        //Pas le niveau ou pas les Points
        {
            if (_spellPts < AncLevel)
                if (World.world.getSort(spellID).getStatsByLevel(AncLevel + 1).getReqLevel() > this.getLevel())
                    return false;
        }
        return away;
    }

    public void boostSpellIncarnation() {
        for (Entry<Integer, Spell.SortStats> i : _sorts.entrySet()) {
            if (getSortStatBySortIfHas(i.getValue().getSpell().getId()) == null)
                continue;
            if (learnSpell(i.getValue().getSpell().getId(), i.getValue().getLevel() + 1, true, false, false))
                ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
        }
    }

    public boolean forgetSpell(int spellID) {
        if (getSortStatBySortIfHas(spellID) == null) {
            return false;
        }
        int AncLevel = getSortStatBySortIfHas(spellID).getLevel();
        if (AncLevel <= 1)
            return false;

        if (learnSpell(spellID, 1, true, false, false)) {
            _spellPts += Formulas.spellCost(AncLevel);
            ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
            return true;
        } else {
            return false;
        }
    }

    public void demorph() {
        if (this.getMorphMode()) {
            int morphID = this.getClasse() * 10 + this.getSexe();
            this.setGfxId(morphID);
            SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.getCurMap(), this.getId());
            SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(this.getCurMap(), this);
        }
    }

    public boolean getMorphMode() {
        return _morphMode;
    }

    public int getMorphId() {
        return _morphId;
    }

    public void setMorphId(int id) {
        this._morphId = id;
    }

    public void setFullMorph(int morphid, boolean isLoad, boolean join) {
        if (this.isOnMount()) this.toogleOnMount();
        if (_morphMode && !join)
            unsetFullMorph();
        if (this.isGhost) {
            SocketManager.send(this, "Im1185");
            return;
        }

        Map<String, String> fullMorph = World.world.getFullMorph(morphid);

        if (fullMorph == null) return;

        if (!join) {
            if (!_morphMode) {
                _saveSpellPts = _spellPts;
                _saveSorts.putAll(_sorts);
                _saveSortsPlaces.putAll(_sortsPlaces);
            }
            if (isLoad) {
                _saveSpellPts = _spellPts;
                _saveSorts.putAll(_sorts);
                _saveSortsPlaces.putAll(_sortsPlaces);
            }
        }

        _morphMode = true;
        _sorts.clear();
        _sortsPlaces.clear();
        _spellPts = 0;


        setGfxId(Integer.parseInt(fullMorph.get("gfxid")));
        if (this.fight == null) SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
        parseSpellsFullMorph(fullMorph.get("spells"));
        setMorphId(morphid);

        if (this.getObjetByPos(Constant.ITEM_POS_ARME) != null)
            if (Constant.isIncarnationWeapon(this.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getId()))
                for (int i = 0; i <= this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.STATS_NIVEAU); i++)
                    if (i == 10 || i == 20 || i == 30 || i == 40 || i == 50)
                        boostSpellIncarnation();
        if (this.fight == null) {
            SocketManager.GAME_SEND_ASK(this.getGameClient(), this);
            SocketManager.GAME_SEND_SPELL_LIST(this);
        }


        if (fullMorph.get("vie") != null) {
            try {
                this.maxPdv = Integer.parseInt(fullMorph.get("vie"));
                this.setPdv(this.getMaxPdv());
                this.pa = Integer.parseInt(fullMorph.get("pa"));
                this.pm = Integer.parseInt(fullMorph.get("pm"));
                this.vitalite = Integer.parseInt(fullMorph.get("vitalite"));
                this.sagesse = Integer.parseInt(fullMorph.get("sagesse"));
                this.terre = Integer.parseInt(fullMorph.get("terre"));
                this.feu = Integer.parseInt(fullMorph.get("feu"));
                this.eau = Integer.parseInt(fullMorph.get("eau"));
                this.air = Integer.parseInt(fullMorph.get("air"));
                this.initiative = Integer.parseInt(fullMorph.get("initiative")) + this.sagesse + this.terre + this.feu + this.eau + this.air;
                this.useStats = fullMorph.get("stats").equals("1");
                this.donjon = fullMorph.get("donjon").equals("1");
                this.useCac = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (this.fight == null) SocketManager.GAME_SEND_STATS_PACKET(this);
        if (!join)
            ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
    }

    public boolean isMorph() {
        return (this.gfxId != 8004 && this.gfxId != (this.getClasse() * 10 + this.getSexe()));
    }

    public boolean canCac() {
        return this.useCac;
    }

    public void unsetMorph() {
        this.setGfxId(this.getClasse() * 10 + this.getSexe());
        SocketManager.GAME_SEND_ALTER_GM_PACKET(this.curMap, this);
        ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
    }

    public void unsetFullMorph() {
        if (!_morphMode)
            return;

        int morphID = this.getClasse() * 10 + this.getSexe();
        setGfxId(morphID);

        useStats = false;
        donjon = false;
        _morphMode = false;
        this.useCac = true;
        _sorts.clear();
        _sortsPlaces.clear();
        _spellPts = _saveSpellPts;
        _sorts.putAll(_saveSorts);
        _sortsPlaces.putAll(_saveSortsPlaces);
        parseSpells(encodeSpellsToDB(), true);

        setMorphId(0);
        if (this.getFight() == null) {
            SocketManager.GAME_SEND_SPELL_LIST(this);
            SocketManager.GAME_SEND_STATS_PACKET(this);
            SocketManager.GAME_SEND_ALTER_GM_PACKET(this.curMap, this);
        }
        ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
    }

    public String encodeSpellListForSL() {
        return _sorts.values().stream().map(s -> {
            // Official servers send position 126 for spells without shortcuts
            int pos = Optional.ofNullable(_sortsPlaces.get(s.getSpellID())).orElse(126);
            return String.join("~",
                String.valueOf(s.getSpellID()),
                String.valueOf(s.getLevel()),
                Integer.toHexString(pos)
            );
        }).collect(Collectors.joining(";"));
    }

    public void setSpellShortcuts(int spellId, int position) {
        removeSpellShortcutAtPosition(position);
        _sortsPlaces.remove(spellId);
        if(position <= 30) _sortsPlaces.put(spellId, position);
        ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
    }

    public void removeSpellShortcutAtPosition(int position) {
        _sorts.keySet().stream()
            .map(_sortsPlaces::get)
            .filter(Objects::nonNull)
            .filter(p -> p == position)
            .forEach(_sortsPlaces::remove);
    }

    public Spell.SortStats getSortStatBySortIfHas(int spellID) {
        return _sorts.get(spellID);
    }

    public String parseALK() {
        StringBuilder perso = new StringBuilder();
        perso.append("|");
        perso.append(this.getId()).append(";");
        perso.append(this.getName()).append(";");
        perso.append(this.getLevel()).append(";");
        int gfx = this.gfxId;
        if (this.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF) != null)
            if (this.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF).getTemplate().getId() == 10681)
                gfx = 8037;
        perso.append(gfx).append(";");
        int color1 = this.getColor1(), color2 = this.getColor2(), color3 = this.getColor3();
        if (this.getObjetByPos(Constant.ITEM_POS_MALEDICTION) != null)
            if (this.getObjetByPos(Constant.ITEM_POS_MALEDICTION).getTemplate().getId() == 10838) {
                color1 = 16342021;
                color2 = 16342021;
                color3 = 16342021;
            }
        perso.append((color1 != -1 ? Integer.toHexString(color1) : "-1")).append(";");
        perso.append((color2 != -1 ? Integer.toHexString(color2) : "-1")).append(";");
        perso.append((color3 != -1 ? Integer.toHexString(color3) : "-1")).append(";");
        perso.append(getGMStuffString()).append(";");
        perso.append((this.isShowSeller() ? 1 : 0)).append(";");
        perso.append(Config.gameServerId).append(";");

        if (this.dead == 1 && Config.modeHeroic) {
            perso.append(this.dead).append(";").append(this.deathCount);
        } else {
            perso.append(0);
        }
        return perso.toString();
    }

    public void remove() {
        ((PlayerData) DatabaseManager.get(PlayerData.class)).delete(this);
    }

    public void OnJoinGame() {
        this._isClone = false;
        getAccount().setCurrentPlayer(this);
        this.setOnline(true);

        if (getAccount().getGameClient() == null)
            return;

        GameClient client = getAccount().getGameClient();

        if(Config.modeHeroic) {
            this.alignment = 0;
            Optional<Player> p = new ArrayList<>(World.world.getOnlinePlayers()).stream().filter(p1 -> p1 != null && p1.getAlignment() > 0
                    && p1.getAccount() != null && p1.getAccount().getCurrentIp().equalsIgnoreCase(getAccount().getCurrentIp()))
                    .findFirst();
            if(p != null) {
                p.ifPresent(player -> {
                    this.alignment = player.alignment;
                    if(this.alignment == Constant.ALIGNEMENT_BONTARIEN) Main.angels++;
                    else if(player.getAlignment() == Constant.ALIGNEMENT_BRAKMARIEN) Main.demons++;
                });
            }
            if(this.alignment <= 0) {
                if (Main.angels > Main.demons) {
                    this.alignment = Constant.ALIGNEMENT_BRAKMARIEN;
                    Main.demons++;
                } else {
                    this.alignment = Constant.ALIGNEMENT_BONTARIEN;
                    Main.angels++;
                }
            }
            this.setShowWings(true);
            SocketManager.GAME_SEND_ZC_PACKET(this, this.alignment);
        }
        if (this._seeSeller) {
            this._seeSeller = false;
            World.world.removeSeller(this.getId(), this.getCurMap().getId());
            SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
        }

        if (this._mount != null)
            SocketManager.GAME_SEND_Re_PACKET(this, "+", this._mount);

        SocketManager.GAME_SEND_Rx_PACKET(this);
        SocketManager.GAME_SEND_ASK(client, this);

        for (int a = 1; a < World.world.getItemSetNumber(); a++)
            if (this.getNumbEquipedItemOfPanoplie(a) != 0)
                SocketManager.GAME_SEND_OS_PACKET(this, a);

        if (this._metiers.size() > 0) {
            ArrayList<JobStat> list = new ArrayList<>();
            list.addAll(this._metiers.values());
            //packet JS
            SocketManager.GAME_SEND_JS_PACKET(this, list);
            //packet JX
            SocketManager.GAME_SEND_JX_PACKET(this, list);
            //Packet JO (Job Option)
            SocketManager.GAME_SEND_JO_PACKET(this, list);
            GameObject obj = getObjetByPos(Constant.ITEM_POS_ARME);
            if (obj != null)
                for (JobStat sm : list)
                    if (sm.getTemplate().isValidTool(obj.getTemplate().getId()))
                        SocketManager.GAME_SEND_OT_PACKET(getAccount().getGameClient(), sm.getTemplate().getId());
        }

        SocketManager.GAME_SEND_ALIGNEMENT(client, alignment);
        SocketManager.GAME_SEND_ADD_CANAL(client, _canaux + "^" + (this.getGroup() != null ? "@" : ""));
        if (_guildMember != null)
            SocketManager.GAME_SEND_gS_PACKET(this, _guildMember);
        SocketManager.GAME_SEND_ZONE_ALLIGN_STATUT(client);
        sendItemShortcuts();
        SocketManager.GAME_SEND_EMOTE_LIST(this, getCompiledEmote(this.emotes));
        SocketManager.GAME_SEND_RESTRICTIONS(client);
        SocketManager.GAME_SEND_Ow_PACKET(this);
        SocketManager.GAME_SEND_SEE_FRIEND_CONNEXION(client, _showFriendConnection);
        SocketManager.GAME_SEND_SPELL_LIST(this);
        getAccount().sendOnline();

        //Messages de bienvenue
        SocketManager.GAME_SEND_Im_PACKET(this, "189");
        if (getAccount().getLastConnectionDate() != null && !getAccount().getLastConnectionDate().equals("") && !getAccount().getLastIP().equals(""))
            SocketManager.GAME_SEND_Im_PACKET(this, "0152;" + getAccount().getLastConnectionDate() + "~" + getAccount().getLastIP());

        SocketManager.GAME_SEND_Im_PACKET(this, "0153;" + getAccount().getCurrentIp());

        getAccount().setLastIP(getAccount().getCurrentIp());

        //Mise a jour du lastConnectionDate
        Date actDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd");
        String jour = dateFormat.format(actDate);
        dateFormat = new SimpleDateFormat("MM");
        String mois = dateFormat.format(actDate);
        dateFormat = new SimpleDateFormat("yyyy");
        String annee = dateFormat.format(actDate);
        dateFormat = new SimpleDateFormat("HH");
        String heure = dateFormat.format(actDate);
        dateFormat = new SimpleDateFormat("mm");
        String min = dateFormat.format(actDate);
        getAccount().setLastConnectionDate(annee + "~" + mois + "~" + jour + "~" + heure + "~" + min);
        if (_guildMember != null)
            _guildMember.setLastCo(annee + "~" + mois + "~" + jour + "~" + heure + "~" + min);
        //Affichage des prismes
        World.world.showPrismes(this);
        //Actualisation dans la DB
        ((AccountData) DatabaseManager.get(AccountData.class)).updateLastConnection(getAccount());
        SocketManager.GAME_SEND_MESSAGE(this, Config.startMessage.isEmpty() ? this.getLang().trans("client.player.onjoingame.startmessage") : Config.startMessage);
        for (GameObject object : this.objects.values()) {
            if (object.getTemplate().getType() == Constant.ITEM_TYPE_FAMILIER) {
                PetEntry p = World.world.getPetsEntry(object.getGuid());
                Pet pets = World.world.getPets(object.getTemplate().getId());

                if (p == null || pets == null) {
                    if (p != null && p.getPdv() > 0)
                        SocketManager.GAME_SEND_Im_PACKET(this, "025");
                    continue;
                }
                if (pets.getType() == 0 || pets.getType() == 1)
                    continue;
                p.updatePets(this, Integer.parseInt(pets.getGap().split(",")[1]));
            } else if (object.getTemplate().getId() == 10207) {
                String date = object.getTxtStat().get(Constant.STATS_DATE);
                if (date != null) {
                    if (date.contains("#")) {
                        date = date.split("#")[3];
                    }
                    if (System.currentTimeMillis() - Long.parseLong(date) > 604800000) {
                        object.getTxtStat().clear();
                        object.getTxtStat().putAll(Dopeul.generateStatsTrousseau());
                        SocketManager.GAME_SEND_UPDATE_ITEM(this, object);
                    }
                }
            }
        }

        if (_morphMode)
            setFullMorph(_morphId, true, true);

        if (Config.autoReboot)
            this.send(Reboot.toStr());
        if(Main.fightAsBlocked)
            this.sendServerMessage("You can't fight until new order.");
        EventManager manager = EventManager.getInstance();
        if(manager.getCurrentEvent() != null && manager.getState() == EventManager.State.PROCESSED)
            this.sendMessage(this.getLang().trans("client.player.event.start.join", manager.getCurrentEvent().getEventName()));

        this.checkVote();

        World.world.logger.info("The player " + this.getName() + " come to connect.");

        if(this.isMorph())
            this.send("AR3K");
        if (this.getEnergy() == 0)
            this.setGhost();
        if (this.fight != null) SocketManager.send(this, "ILF0");
        else SocketManager.send(this, "ILS2000");
    }

    public void checkVote() {
        String IP = this.getAccount().getLastIP();
        long now = System.currentTimeMillis() / 1000;
        boolean vote = true;
        for (Account account : World.world.getAccounts()) {
            if (account != null && getAccount().getLastVoteIP() != null && !getAccount().getLastVoteIP().equalsIgnoreCase("")) {
                if (getAccount().getLastVoteIP().equalsIgnoreCase(IP)) {
                    if ((getAccount().getHeureVote() + 3600 * 3) > now) {
                        vote = false;
                        break;
                    }
                }
            }
        }

        if (vote) this.send("Im116;<b>Server</b>~" + this.getLang().trans("command.commandplayer.vote.ok"));
    }

    public void SetSeeFriendOnline(boolean bool) {
        _showFriendConnection = bool;
    }

    public void sendGameCreate() {
        this.setOnline(true);
        getAccount().setCurrentPlayer(this);

        if (getAccount().getGameClient() == null)
            return;

        GameClient client = getAccount().getGameClient();
        SocketManager.GAME_SEND_GAME_CREATE(client, this.getName());
        SocketManager.GAME_SEND_STATS_PACKET(this);
        ((PlayerData) DatabaseManager.get(PlayerData.class)).updateLogged(this.id, 1);
        this.verifEquiped();

        if (this.getLastFight() == null) {
            SocketManager.GAME_SEND_MAPDATA(client, this.curMap.getId(), this.curMap.getDate(), this.curMap.getKey());
            SocketManager.GAME_SEND_MAP_FIGHT_COUNT(client, this.getCurMap());
            if (this.getFight() == null) this.curMap.addPlayer(this);
        } else {
            try {
                client.parsePacket("GI");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String parseToOa() {
        return "Oa" + this.getId() + "|" + getGMStuffString();
    }

    public String parseToGM() {
        StringBuilder str = new StringBuilder();
        if (fight == null && curCell != null)// Hors combat
        {
            str.append(curCell.getId()).append(";").append(_orientation).append(";");
            str.append("0").append(";");//FIXME:?
            str.append(this.getId()).append(";").append(this.getName()).append(";").append(this.getClasse());
            str.append((this.getCurrentTitle() > 0 ? ("," + this.getCurrentTitle() + ";") : (";")));
            int gfx = gfxId;
            if (this.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF) != null)
                if (this.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF).getTemplate().getId() == 10681)
                    gfx = 8037;
            str.append(gfx).append("^").append(_size);//gfxID^size

            if (this.getObjetByPos(Constant.ITEM_POS_PNJ_SUIVEUR) != null) {
                str.append(",").append(Constant.getItemIdByMascotteId(this.getObjetByPos(Constant.ITEM_POS_PNJ_SUIVEUR).getTemplate().getId())).append("^100");
            }

            str.append(";").append(this.getSexe()).append(";");
            str.append(alignment).append(",");
            str.append("0").append(",");//FIXME:?
            str.append((_showWings ? getGrade() : "0")).append(",");
            str.append(this.getLevel() + this.getId());
            if (_showWings && _deshonor > 0) {
                str.append(",1;");
            } else {
                str.append(";");
            }
            int color1 = this.getColor1(), color2 = this.getColor2(), color3 = this.getColor3();
            if (this.getObjetByPos(Constant.ITEM_POS_MALEDICTION) != null)
                if (this.getObjetByPos(Constant.ITEM_POS_MALEDICTION).getTemplate().getId() == 10838) {
                    color1 = 16342021;
                    color2 = 16342021;
                    color3 = 16342021;
                }

            str.append((color1 == -1 ? "-1" : Integer.toHexString(color1))).append(";");
            str.append((color2 == -1 ? "-1" : Integer.toHexString(color2))).append(";");
            str.append((color3 == -1 ? "-1" : Integer.toHexString(color3))).append(";");
            str.append(getGMStuffString()).append(";");
            if (hasEquiped(10054) || hasEquiped(10055) || hasEquiped(10056)
                    || hasEquiped(10058) || hasEquiped(10061)
                    || hasEquiped(10102)) {
                str.append(3).append(";");
                setCurrentTitle(2);
            } else {
                if (getCurrentTitle() == 2)
                    setCurrentTitle(0);
                Group g = this.getGroup();
                int level = this.getLevel();
                if (g != null)
                    if (!g.isPlayer() || this.get_size() <= 0) // Si c'est un groupe non joueur ou que l'on est invisible on cache l'aura
                        level = 1;
                str.append((level > 99 ? (level > 199 ? (2) : (1)) : (0))).append(";");
            }
            str.append(";");//Emote
            str.append(";");//Emote timer
            if (this._guildMember != null
                    && this._guildMember.getGuild().haveTenMembers())
                str.append(this._guildMember.getGuild().getName()).append(";").append(this._guildMember.getGuild().getEmblem()).append(";");
            else
                str.append(";;");
            if (this.dead == 1 && !this.isGhost)
                str.append("-1");
            str.append(getSpeed()).append(";");//Restriction
            str.append((_onMount && _mount != null ? _mount.getStringColor(parsecolortomount()) : "")).append(";");
            str.append(this.isDead()).append(";");
        }
        return str.toString();
    }

    public String parseToMerchant() {
        StringBuilder str = new StringBuilder();
        str.append(curCell.getId()).append(";");
        str.append(_orientation).append(";");
        str.append("0").append(";");
        str.append(this.getId()).append(";");
        str.append(this.getName()).append(";");
        str.append("-5").append(";");//Merchant identifier
        str.append(gfxId).append("^").append(_size).append(";");
        int color1 = this.getColor1(), color2 = this.getColor2(), color3 = this.getColor3();
        if (this.getObjetByPos(Constant.ITEM_POS_MALEDICTION) != null)
            if (this.getObjetByPos(Constant.ITEM_POS_MALEDICTION).getTemplate().getId() == 10838) {
                color1 = 16342021;
                color2 = 16342021;
                color3 = 16342021;
            }
        str.append((color1 == -1 ? "-1" : Integer.toHexString(color1))).append(";");
        str.append((color2 == -1 ? "-1" : Integer.toHexString(color2))).append(";");
        str.append((color3 == -1 ? "-1" : Integer.toHexString(color3))).append(";");
        str.append(getGMStuffString()).append(";");//acessories
        str.append((_guildMember != null ? _guildMember.getGuild().getName() : "")).append(";");//guildName
        str.append((_guildMember != null ? _guildMember.getGuild().getEmblem() : "")).append(";");//emblem
        str.append("0;");//offlineType
        return str.toString();
    }

    public String getGMStuffString() {
        StringBuilder str = new StringBuilder();

        GameObject object = getObjetByPos(Constant.ITEM_POS_ARME);

        if (object != null)
            str.append(Integer.toHexString(object.getAppearanceTemplateId()));

        str.append(",");

        object = getObjetByPos(Constant.ITEM_POS_COIFFE);

        if (object != null) {
            object.encodeStats();

            Integer obvi = object.getStats().getEffects().get(970);
            if (obvi == null) {
                str.append(Integer.toHexString(object.getAppearanceTemplateId()));
            } else {
                str.append(Integer.toHexString(obvi)).append("~16~").append(object.getObvijevanLook());
            }
        }

        str.append(",");

        object = getObjetByPos(Constant.ITEM_POS_CAPE);

        if (object != null) {
            object.encodeStats();

            Integer obvi = object.getStats().getEffects().get(970);
            if (obvi == null) {
                str.append(Integer.toHexString(object.getAppearanceTemplateId()));
            } else {
                str.append(Integer.toHexString(obvi)).append("~17~").append(object.getObvijevanLook());
            }
        }

        str.append(",");

        object = getObjetByPos(Constant.ITEM_POS_FAMILIER);

        if (object != null)
            str.append(Integer.toHexString(object.getAppearanceTemplateId()));

        str.append(",");

        object = getObjetByPos(Constant.ITEM_POS_BOUCLIER);

        if (object != null)
            str.append(Integer.toHexString(object.getAppearanceTemplateId()));

        return str.toString();
    }

    public String getAsPacket() {
        refreshStats();
        refreshLife(true);
        StringBuilder ASData = new StringBuilder();
        ASData.append("As").append(xpString(",")).append("|");
        ASData.append(kamas);
        ASData.append("|").append(_capital).append("|").append(_spellPts).append("|");
        ASData.append(alignment).append("~").append(alignment).append(",").append(_aLvl).append(",").append(getGrade()).append(",").append(_honor).append(",").append(_deshonor).append(",").append((_showWings ? "1" : "0")).append("|");
        int pdv = this.curPdv;
        int pdvMax = this.maxPdv;
        if (fight != null && !fight.isFinish()) {
            Fighter f = fight.getFighterByPerso(this);
            if (f != null) {
                pdv = f.getPdv();
                pdvMax = f.getPdvMax();
            }
        }
        Stats stats = this.getStats(), sutffStats = this.getStuffStats(), donStats = this.getDonsStats(), buffStats = this.getBuffsStats(), totalStats = this.getTotalStats(false);

        ASData.append(pdv).append(",").append(pdvMax).append("|");
        ASData.append(this.getEnergy()).append(",10000|");
        ASData.append(getInitiative()).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_PROS) + sutffStats.getEffect(Constant.STATS_ADD_PROS) + ((int) Math.ceil(totalStats.getEffect(Constant.STATS_ADD_CHAN) / 10)) + buffStats.getEffect(Constant.STATS_ADD_PROS) + ((int) Math.ceil(buffStats.getEffect(Constant.STATS_ADD_CHAN) / 10))).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_PA)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_PA)).append(",").append(donStats.getEffect(Constant.STATS_ADD_PA)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_PA)).append(",").append(totalStats.getEffect(Constant.STATS_ADD_PA)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_PM)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_PM)).append(",").append(donStats.getEffect(Constant.STATS_ADD_PM)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_PM)).append(",").append(totalStats.getEffect(Constant.STATS_ADD_PM)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_FORC)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_FORC)).append(",").append(donStats.getEffect(Constant.STATS_ADD_FORC)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_FORC)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_VITA)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_VITA)).append(",").append(donStats.getEffect(Constant.STATS_ADD_VITA)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_VITA)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_SAGE)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_SAGE)).append(",").append(donStats.getEffect(Constant.STATS_ADD_SAGE)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_SAGE)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_CHAN)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_CHAN)).append(",").append(donStats.getEffect(Constant.STATS_ADD_CHAN)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_CHAN)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_AGIL)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_AGIL)).append(",").append(donStats.getEffect(Constant.STATS_ADD_AGIL)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_AGIL)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_INTE)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_INTE)).append(",").append(donStats.getEffect(Constant.STATS_ADD_INTE)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_INTE)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_PO)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_PO)).append(",").append(donStats.getEffect(Constant.STATS_ADD_PO)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_PO)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_CREATURE)).append(",").append(sutffStats.getEffect(Constant.STATS_CREATURE)).append(",").append(donStats.getEffect(Constant.STATS_CREATURE)).append(",").append(buffStats.getEffect(Constant.STATS_CREATURE)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_DOMA)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_DOMA)).append(",").append(donStats.getEffect(Constant.STATS_ADD_DOMA)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_DOMA)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_PDOM)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_PDOM)).append(",").append(donStats.getEffect(Constant.STATS_ADD_PDOM)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_PDOM)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_MAITRISE)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_MAITRISE)).append(",").append(donStats.getEffect(Constant.STATS_ADD_MAITRISE)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_MAITRISE)).append("|");//ASData.append("0,0,0,0|");//Maitrise ?
        ASData.append(stats.getEffect(Constant.STATS_ADD_PERDOM)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_PERDOM)).append(",").append(donStats.getEffect(Constant.STATS_ADD_PERDOM)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_PERDOM)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_SOIN)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_SOIN)).append(",").append(donStats.getEffect(Constant.STATS_ADD_SOIN)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_SOIN)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_TRAP_DOM)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_TRAP_DOM)).append(",").append(donStats.getEffect(Constant.STATS_ADD_TRAP_DOM)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_TRAP_DOM)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_TRAP_PERDOM)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_TRAP_PERDOM)).append(",").append(donStats.getEffect(Constant.STATS_ADD_TRAP_PERDOM)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_TRAP_PERDOM)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_RETDOM)).append(",").append(sutffStats.getEffect(Constant.STATS_RETDOM)).append(",").append(donStats.getEffect(Constant.STATS_RETDOM)).append(",").append(buffStats.getEffect(Constant.STATS_RETDOM)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_CC)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_CC)).append(",").append(donStats.getEffect(Constant.STATS_ADD_CC)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_CC)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_EC)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_EC)).append(",").append(donStats.getEffect(Constant.STATS_ADD_EC)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_EC)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_AFLEE)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_AFLEE)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_AFLEE)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_AFLEE)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_MFLEE)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_MFLEE)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_MFLEE)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_MFLEE)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_R_NEU)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_R_NEU)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_NEU)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_NEU)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_RP_NEU)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_RP_NEU)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_NEU)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_NEU)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_R_PVP_NEU)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_R_PVP_NEU)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_PVP_NEU)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_PVP_NEU)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_RP_PVP_NEU)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_RP_PVP_NEU)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_PVP_NEU)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_PVP_NEU)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_R_TER)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_R_TER)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_TER)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_TER)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_RP_TER)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_RP_TER)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_TER)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_TER)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_R_PVP_TER)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_R_PVP_TER)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_PVP_TER)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_PVP_TER)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_RP_PVP_TER)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_RP_PVP_TER)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_PVP_TER)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_PVP_TER)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_R_EAU)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_R_EAU)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_EAU)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_EAU)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_RP_EAU)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_RP_EAU)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_EAU)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_EAU)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_R_PVP_EAU)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_R_PVP_EAU)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_PVP_EAU)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_PVP_EAU)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_RP_PVP_EAU)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_RP_PVP_EAU)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_PVP_EAU)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_PVP_EAU)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_R_AIR)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_R_AIR)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_AIR)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_AIR)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_RP_AIR)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_RP_AIR)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_AIR)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_AIR)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_R_PVP_AIR)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_R_PVP_AIR)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_PVP_AIR)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_PVP_AIR)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_RP_PVP_AIR)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_RP_PVP_AIR)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_PVP_AIR)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_PVP_AIR)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_R_FEU)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_R_FEU)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_FEU)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_FEU)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_RP_FEU)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_RP_FEU)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_FEU)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_FEU)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_R_PVP_FEU)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_R_PVP_FEU)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_PVP_FEU)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_PVP_FEU)).append("|");
        ASData.append(stats.getEffect(Constant.STATS_ADD_RP_PVP_FEU)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_RP_PVP_FEU)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_PVP_FEU)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_PVP_FEU)).append("|");
        return ASData.toString();
    }

    public int getGrade() {
        if (alignment == Constant.ALIGNEMENT_NEUTRE)
            return 0;
        if (_honor >= 17500)
            return 10;
        return World.world.getExperiences().pvp.levelForXp(_honor);
    }

    public String xpString(String c) {
        if (!_morphMode) {
            ExperienceTables.ExperienceTable xpTable = World.world.getExperiences().players;
            return this.getExp() + c + xpTable.minXpAt(this.getLevel()) + c + xpTable.maxXpAt(this.getLevel());
        }
        if(this.getObjetByPos(Constant.ITEM_POS_ARME) == null
                || !Constant.isIncarnationWeapon(this.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getId())
                || this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.ERR_STATS_XP) == null) {
            return 1 + c + 1 + c + 1;
        }

        // What if it's a tormentator ?
        ExperienceTables.ExperienceTable xpTable = World.world.getExperiences().bandits;
        int level = this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.STATS_NIVEAU);
        return this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.ERR_STATS_XP)
            + c
            + xpTable.minXpAt(level)
            + c
            + xpTable.maxXpAt(level);
    }

    public int emoteActive() {
        return _emoteActive;
    }

    public void setEmoteActive(int emoteActive) {
        this._emoteActive = emoteActive;
    }

    public Stats getStuffStats() {
        if (this.useStats) return new Stats();

        Stats stats = new Stats(false, null);
        ArrayList<Integer> itemSetApplied = new ArrayList<>();
        synchronized(objects) {
            for (GameObject gameObject : new ArrayList<>(this.objects.values())) {
                byte position = (byte) gameObject.getPosition();
                if (position != Constant.ITEM_POS_NO_EQUIPED) {
                    if (position >= 35 && position <= 48)
                        continue;

                    stats = Stats.cumulStat(stats, gameObject.getStats());
                    int id = gameObject.getTemplate().getPanoId();

                    if (id > 0 && !itemSetApplied.contains(id)) {
                        itemSetApplied.add(id);
                        ObjectSet objectSet = World.world.getItemSet(id);
                        if (objectSet != null)
                            stats = Stats.cumulStat(stats, objectSet.getBonusStatByItemNumb(this.getNumbEquipedItemOfPanoplie(id)));
                    }
                }
            }
        }

        if (this._mount != null && this._onMount)
            stats = Stats.cumulStat(stats, this._mount.getStats());

        return stats;
    }

    public Stats getBuffsStats() {
        Stats stats = new Stats(false, null);
        if (this.fight != null)
            if (this.fight.getFighterByPerso(this) != null)
                for (SpellEffect entry : this.fight.getFighterByPerso(this).getFightBuff())
                    stats.addOneStat(entry.getEffectID(), entry.getValue());

        for (Entry<Integer, SpellEffect> entry : buffs.entrySet())
            stats.addOneStat(entry.getValue().getEffectID(), entry.getValue().getValue());
        return stats;
    }

    public int get_orientation() {
        return _orientation;
    }

    public void set_orientation(int _orientation) {
        this._orientation = _orientation;
    }

    public int getInitiative() {
        if (!useStats) {
            int fact = 4;
            int maxPdv = this.maxPdv - 55;
            int curPdv = this.curPdv - 55;
            if (this.getClasse() == Constant.CLASS_SACRIEUR)
                fact = 8;
            double coef = maxPdv / fact;

            coef += getStuffStats().getEffect(Constant.STATS_ADD_INIT);
            coef += getTotalStats(false).getEffect(Constant.STATS_ADD_AGIL);
            coef += getTotalStats(false).getEffect(Constant.STATS_ADD_CHAN);
            coef += getTotalStats(false).getEffect(Constant.STATS_ADD_INTE);
            coef += getTotalStats(false).getEffect(Constant.STATS_ADD_FORC);

            int init = 1;
            if (maxPdv != 0)
                init = (int) (coef * ((double) curPdv / (double) maxPdv));
            if (init < 0)
                init = 0;
            return init;
        } else {
            return this.initiative;
        }
    }

    public Stats getTotalStats(boolean lessBuff) {
        Stats total = new Stats(false, null);
        if (!useStats) {
            total = Stats.cumulStat(total, this.getStats());
            total = Stats.cumulStat(total, this.getStuffStats());
            total = Stats.cumulStat(total, this.getDonsStats());
            if (fight != null && !lessBuff)
                total = Stats.cumulStat(total, this.getBuffsStats());
        } else {
            return newStatsMorph();
        }
        return total;
    }

    public Stats getDonsStats() {
        Stats stats = new Stats(false, null);
        return stats;
    }

    public Stats newStatsMorph() {
        Stats stats = new Stats();
        stats.addOneStat(Constant.STATS_ADD_PA, this.pa);
        stats.addOneStat(Constant.STATS_ADD_PM, this.pm);
        stats.addOneStat(Constant.STATS_ADD_VITA, this.vitalite);
        stats.addOneStat(Constant.STATS_ADD_SAGE, this.sagesse);
        stats.addOneStat(Constant.STATS_ADD_FORC, this.terre);
        stats.addOneStat(Constant.STATS_ADD_INTE, this.feu);
        stats.addOneStat(Constant.STATS_ADD_CHAN, this.eau);
        stats.addOneStat(Constant.STATS_ADD_AGIL, this.air);
        stats.addOneStat(Constant.STATS_ADD_INIT, this.initiative);
        stats.addOneStat(Constant.STATS_ADD_PROS, 100);
        stats.addOneStat(Constant.STATS_CREATURE, 1);
        this.useCac = false;
        return stats;
    }

    public int getPodUsed() {
        int pod = 0;

        for (Entry<Integer, GameObject> entry : objects.entrySet()) {
            if(entry.getValue() != null)
                pod += entry.getValue().getTemplate().getPod() * entry.getValue().getQuantity();
        }

        pod += parseStoreItemsListPods();
        return pod;
    }

    public int getMaxPod() {
        Stats total = new Stats(false, null);
        total = Stats.cumulStat(total, this.getStats());
        total = Stats.cumulStat(total, this.getStuffStats());
        total = Stats.cumulStat(total, this.getDonsStats());
        int pods = total.getEffect(Constant.STATS_ADD_PODS);
        pods += total.getEffect(Constant.STATS_ADD_FORC) * 5;
        for (JobStat SM : _metiers.values()) {
            pods += SM.get_lvl() * 5;
            if (SM.get_lvl() == 100)
                pods += 1000;
        }
        if (pods < 1000)
            pods = 1000;
        return pods + 9000;
    }

    public void refreshLife(boolean refresh) {
        if (get_isClone())
            return;
        long time = (System.currentTimeMillis() - regenTime);
        regenTime = System.currentTimeMillis();
        if (fight != null)
            return;
        if (regenRate == 0)
            return;
        if (this.curPdv > this.maxPdv) {
            this.curPdv = this.maxPdv - 1;
            if (!refresh)
                SocketManager.GAME_SEND_STATS_PACKET(this);
            return;
        }

        int diff = (int) time / regenRate;
        if (diff >= 1 && this.curPdv < this.maxPdv && refresh) {
            SocketManager.send(this, "ILF" + diff);
            SocketManager.send(this, "ILS" + regenRate);
        }

        setPdv(this.curPdv + diff);
    }

    public int getAlignment() {
        return alignment;
    }

    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    public int get_pdvper() {
        refreshLife(false);
        int pdvper = 100;
        pdvper = (100 * this.curPdv) / this.maxPdv;
        if (pdvper > 100)
            return 100;
        return pdvper;
    }

    public void useSmiley(String str) {
        try {
            int id = Integer.parseInt(str);
            GameMap map = curMap;
            if (fight == null)
                SocketManager.GAME_SEND_EMOTICONE_TO_MAP(map, this.getId(), id);
            else
                SocketManager.GAME_SEND_EMOTICONE_TO_FIGHT(fight, 7, this.getId(), id);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void boostStat(int stat, boolean capital) {
        int value = 0;
        switch (stat) {
            case 10://Force
                value = this.getStats().getEffect(Constant.STATS_ADD_FORC);
                break;
            case 13://Chance
                value = this.getStats().getEffect(Constant.STATS_ADD_CHAN);
                break;
            case 14://Agilit�
                value = this.getStats().getEffect(Constant.STATS_ADD_AGIL);
                break;
            case 15://Intelligence
                value = this.getStats().getEffect(Constant.STATS_ADD_INTE);
                break;
        }
        int cout = Constant.getReqPtsToBoostStatsByClass(this.getClasse(), stat, value);
        if (!capital)
            cout = 0;
        if (cout <= _capital) {
            switch (stat) {
                case 11://Vita
                    if (this.getClasse() != Constant.CLASS_SACRIEUR)
                        this.getStats().addOneStat(Constant.STATS_ADD_VITA, 1);
                    else
                        this.getStats().addOneStat(Constant.STATS_ADD_VITA, capital ? 2 : 1);
                    break;
                case 12://Sage
                    this.getStats().addOneStat(Constant.STATS_ADD_SAGE, 1);
                    break;
                case 10://Force
                    this.getStats().addOneStat(Constant.STATS_ADD_FORC, 1);
                    break;
                case 13://Chance
                    this.getStats().addOneStat(Constant.STATS_ADD_CHAN, 1);
                    break;
                case 14://Agilit�
                    this.getStats().addOneStat(Constant.STATS_ADD_AGIL, 1);
                    break;
                case 15://Intelligence
                    this.getStats().addOneStat(Constant.STATS_ADD_INTE, 1);
                    break;
                default:
                    return;
            }
            _capital -= cout;
            SocketManager.GAME_SEND_STATS_PACKET(this);
            ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
        }
    }

    public void boostStatFixedCount(int stat, int countVal) {
        for (int i = 0; i < countVal; i++) {
            int value = 0;
            switch (stat) {
                case 10://Force
                    value = this.getStats().getEffect(Constant.STATS_ADD_FORC);
                    break;
                case 13://Chance
                    value = this.getStats().getEffect(Constant.STATS_ADD_CHAN);
                    break;
                case 14://Agilit�
                    value = this.getStats().getEffect(Constant.STATS_ADD_AGIL);
                    break;
                case 15://Intelligence
                    value = this.getStats().getEffect(Constant.STATS_ADD_INTE);
                    break;
            }
            int cout = Constant.getReqPtsToBoostStatsByClass(this.getClasse(), stat, value);
            if (cout <= _capital) {
                switch (stat) {
                    case 11://Vita
                        if (this.getClasse() != Constant.CLASS_SACRIEUR)
                            this.getStats().addOneStat(Constant.STATS_ADD_VITA, 1);
                        else
                            this.getStats().addOneStat(Constant.STATS_ADD_VITA, 2);
                        break;
                    case 12://Sage
                        this.getStats().addOneStat(Constant.STATS_ADD_SAGE, 1);
                        break;
                    case 10://Force
                        this.getStats().addOneStat(Constant.STATS_ADD_FORC, 1);
                        break;
                    case 13://Chance
                        this.getStats().addOneStat(Constant.STATS_ADD_CHAN, 1);
                        break;
                    case 14://Agilit�
                        this.getStats().addOneStat(Constant.STATS_ADD_AGIL, 1);
                        break;
                    case 15://Intelligence
                        this.getStats().addOneStat(Constant.STATS_ADD_INTE, 1);
                        break;
                    default:
                        return;
                }
                _capital -= cout;
            }
        }
        SocketManager.GAME_SEND_STATS_PACKET(this);
        ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
    }

    public boolean isMuted() {
        return getAccount().isMuted();
    }

    public String parseObjetsToDB() {
        StringBuilder str = new StringBuilder();
        if (objects.isEmpty())
            return "";
        for (Entry<Integer, GameObject> entry : objects.entrySet()) {
            GameObject obj = entry.getValue();
            if (obj == null)
                continue;
            str.append(obj.getGuid()).append("|");
        }

        return str.toString();
    }

    public void addItem(int templateId, int quantity, boolean useMax, boolean display) {
        this.addItem(World.world.getObjTemplate(templateId), quantity, useMax, display);
    }

    public void addItem(ObjectTemplate template, int quantity, boolean useMax, boolean display) {
        GameObject item = template.createNewItem(quantity, useMax);
        if (this.addItem(item, true, display)) {
            World.world.addGameObject(item);
        }
    }

    public boolean addItem(GameObject newItem, boolean stack, boolean display) {
        synchronized (objects) {
            for (GameObject item : objects.values()) {
                if (World.world.getConditionManager().stackIfSimilar(item, newItem, stack)) {
                    item.setQuantity(item.getQuantity() + newItem.getQuantity());
                    if (isOnline) {
                        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, item);
                        SocketManager.GAME_SEND_Ow_PACKET(this);
                        if(display) {
                            SocketManager.GAME_SEND_Im_PACKET(this, "021;" + newItem.getQuantity() + "~" + newItem.getTemplate());
                        }
                    }
                    return false;
                }
            }
            addItem(newItem, display);
        }
        return true;
    }

    public void addItem(GameObject item, boolean display) {
        this.objects.put(item.getGuid(), item);
        if(isOnline) {
            SocketManager.GAME_SEND_OAKO_PACKET(this, item);
            if(display) {
                SocketManager.GAME_SEND_Im_PACKET(this, "021;" + item.getQuantity() + "~" + item.getTemplate());
            }
        }
    }

    public boolean addObjetSimiler(GameObject objet, boolean hasSimiler, int oldID) {
        ObjectTemplate objModelo = objet.getTemplate();
        if (hasSimiler) {
            for (Entry<Integer, GameObject> entry : objects.entrySet()) {
                GameObject obj = entry.getValue();
                if (obj.getPosition() == -1 && obj.getGuid() != oldID
                        && obj.getTemplate().getId() == objModelo.getId()
                        && obj.getStats().isSameStats(objet.getStats())
                        && World.world.getConditionManager().stackIfSimilar(obj, objet, hasSimiler)) {
                    obj.setQuantity(obj.getQuantity() + objet.getQuantity());
                    SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, obj);
                    return true;
                }
            }
        }
        return false;
    }

    public Map<Integer, GameObject> getItems() {
        return objects;
    }

    public String encodeItemASK() {
        StringBuilder str = new StringBuilder();
        if (objects.isEmpty())
            return "";
        for (GameObject obj : objects.values()) {
            str.append(obj.encodeItem());
        }
        return str.toString();
    }

    public String getItemsIDSplitByChar(String splitter) {
        StringBuilder str = new StringBuilder();
        if (objects.isEmpty())
            return "";
        for (int entry : objects.keySet()) {
            if (str.length() != 0)
                str.append(splitter);
            str.append(entry);
        }

        return str.toString();
    }

    public String getStoreItemsIDSplitByChar(String splitter) {
        StringBuilder str = new StringBuilder();
        if (_storeItems.isEmpty())
            return "";
        for (int entry : _storeItems.keySet()) {
            if (str.length() != 0)
                str.append(splitter);
            str.append(entry);
        }
        return str.toString();
    }

    public boolean hasItemGuid(int guid) {
        return objects.get(guid) != null && objects.get(guid).getQuantity() > 0;
    }

    public void sellItem(int guid, int qua) {
        if (qua <= 0)
            return;

        GameObject object = objects.get(guid);
        if (object.getQuantity() < qua)//Si il a moins d'item que ce qu'on veut Del
            qua = object.getQuantity();

        int price = qua * (object.getTemplate().getPrice() / 10);//Calcul du prix de vente (prix d'achat/10)
        int newQua = object.getQuantity() - qua;

        if (newQua <= 0) {
            ((ObjectData) DatabaseManager.get(ObjectData.class)).delete(object);
            objects.remove(guid);
            World.world.removeGameObject(guid);
            SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
        } else {
            objects.get(guid).setQuantity(newQua);
            SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, objects.get(guid));
        }

        kamas = kamas + price;
        SocketManager.GAME_SEND_STATS_PACKET(this);
        SocketManager.GAME_SEND_Ow_PACKET(this);
        SocketManager.GAME_SEND_ESK_PACKEt(this);
    }

    public void removeItem(int guid) {
        synchronized(objects) {
            objects.remove(guid);
        }
    }

    public void removeItem(int guid, int nombre, boolean send,
                           boolean deleteFromWorld) {
        GameObject obj;
        synchronized(objects) {
            obj = objects.get(guid);
        }

        if(obj == null) return;

        if (nombre > obj.getQuantity())
            nombre = obj.getQuantity();

        if (obj.getQuantity() >= nombre) {
            int newQua = obj.getQuantity() - nombre;
            if (newQua > 0) {
                obj.setQuantity(newQua);
                if (send && isOnline)
                    SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, obj);
            } else {
                //on supprime de l'inventaire et du Monde
                synchronized(objects) {
                    objects.remove(obj.getGuid());
                }
                if (deleteFromWorld)
                    World.world.removeGameObject(obj.getGuid());
                //on envoie le packet si connect�
                if (send && isOnline)
                    SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, obj.getGuid());
            }
        }

        SocketManager.GAME_SEND_Ow_PACKET(this);
    }

    public void deleteItem(int guid) {
        synchronized(objects) {
            objects.remove(guid);
        }
        World.world.removeGameObject(guid);
    }

    public GameObject getObjetByPos(int pos) {
        if (pos == Constant.ITEM_POS_NO_EQUIPED)
            return null;
        synchronized(objects) {
            for (GameObject gameObject : this.objects.values()) {
                if (gameObject.getPosition() == pos && pos == Constant.ITEM_POS_FAMILIER) {
                    if (gameObject.getTxtStat().isEmpty()) return null;
                    else if (World.world.getPetsEntry(gameObject.getGuid()) == null) return null;
                }
                if (gameObject.getPosition() == pos) return gameObject;
            }
        }

        return null;
    }

    //TODO: Delete s'te fonction.
    public GameObject getObjetByPos2(int pos) {
        if (pos == Constant.ITEM_POS_NO_EQUIPED)
            return null;

        for (Entry<Integer, GameObject> entry : objects.entrySet()) {
            GameObject obj = entry.getValue();

            if (obj.getPosition() == pos)
                return obj;
        }
        return null;
    }

    public void refreshStats() {
        double actPdvPer = (100 * (double) this.curPdv) / (double) this.maxPdv;
        if (!useStats)
            this.maxPdv = (this.getLevel() - 1) * 5 + 50 + getTotalStats(false).getEffect(Constant.STATS_ADD_VITA);
        this.curPdv = (int) Math.round(maxPdv * actPdvPer / 100);
    }

    public boolean levelUp(boolean send, boolean addXp) {
        ExperienceTables.ExperienceTable xpTable = World.world.getExperiences().players;
        if (this.getLevel() == xpTable.maxLevel())
            return false;
        this.level++;
        _capital += 5;
        _spellPts++;
        this.maxPdv += 5;
        this.setPdv(this.getMaxPdv());
        if (this.getLevel() == 100)
            this.getStats().addOneStat(Constant.STATS_ADD_PA, 1);
        Constant.onLevelUpSpells(this, this.getLevel());
        if (addXp)
            this.exp =  xpTable.minXpAt(this.getLevel());
        if (send && isOnline) {
            SocketManager.GAME_SEND_STATS_PACKET(this);
            SocketManager.GAME_SEND_SPELL_LIST(this);
        }
        return true;
    }

    public boolean addXp(long winxp) {
        ExperienceTables.ExperienceTable xpTable = World.world.getExperiences().players;

        boolean up = false;
        this.exp += winxp;
        while (this.getExp() >= xpTable.maxXpAt(this.level) && this.level < xpTable.maxLevel())
            up = levelUp(true, false);
        if (isOnline) {
            if (up)
                SocketManager.GAME_SEND_NEW_LVL_PACKET(getAccount().getGameClient(), this.getLevel());
            SocketManager.GAME_SEND_STATS_PACKET(this);
        }
        return up;
    }

    public boolean levelUpIncarnations(boolean send, boolean addXp) {
        int level = this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.STATS_NIVEAU);

        if (level == 50)
            return false;

        level++;
        this.setPdv(this.getMaxPdv());
        SocketManager.GAME_SEND_STATS_PACKET(this);

        switch (level) {
            case 10:
            case 20:
            case 30:
            case 40:
            case 50:
                boostSpellIncarnation();
                break;
        }

        if (send && isOnline) {
            SocketManager.GAME_SEND_STATS_PACKET(this);
            SocketManager.GAME_SEND_SPELL_LIST(this);
        }

        this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().clear();
        this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().put(Constant.STATS_NIVEAU, level);
        this.getObjetByPos(Constant.ITEM_POS_ARME);
        SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(this, this.getObjetByPos(Constant.ITEM_POS_ARME));
        return true;
    }

    public boolean addXpIncarnations(long winxp) {
        boolean up = false;
        int level = this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.STATS_NIVEAU);
        long exp = this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.ERR_STATS_XP);
        exp += winxp;

        if (Constant.isBanditsWeapon(this.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getId())) {
            ExperienceTables.ExperienceTable xpTable = World.world.getExperiences().bandits;

            while (exp >= xpTable.maxXpAt(level) && level < xpTable.maxLevel()) {
                up = levelUpIncarnations(true, false);
                level = this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.STATS_NIVEAU);
            }
        } else if (Constant.isTourmenteurWeapon(this.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getId())) {
            ExperienceTables.ExperienceTable xpTable = World.world.getExperiences().tormentators;

            while (exp >= xpTable.maxXpAt(level) && level < xpTable.maxLevel()) {
                up = levelUpIncarnations(true, false);
                level = this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.STATS_NIVEAU);
            }
        }
        if (isOnline)
            SocketManager.GAME_SEND_STATS_PACKET(this);
        level = this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.STATS_NIVEAU);
        this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().clear();
        this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().put(Constant.STATS_NIVEAU, level);
        this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().put(Constant.ERR_STATS_XP, (int) exp);
        return up;
    }

    public boolean addKamas(long l) {
        // Make sure the player has enough
        if(l < 0 && kamas < -l) return false;
        kamas += l;
        return true;
    }

    public boolean modKamasDisplay(long quantity) {
        if(!addKamas(quantity)) {
            if(isOnline)SocketManager.GAME_SEND_Im_PACKET(this, "182");
            return false;
        }
        if(!isOnline)return true;
        if(quantity < 0) {
            SocketManager.GAME_SEND_Im_PACKET(this, "046;" + (-quantity));
        } else {
            SocketManager.GAME_SEND_Im_PACKET(this, "045;" + quantity);
        }
        SocketManager.GAME_SEND_STATS_PACKET(this);
        return true;
    }

    public GameObject getSimilarItem(GameObject exGameObject) {
        if (exGameObject.getTemplate().getId() == 8378)
            return null;
        synchronized(objects) {
            for (GameObject gameObject : this.objects.values())
                if (gameObject.getTemplate().getId() == exGameObject.getTemplate().getId()
                        && World.world.getConditionManager().stackIfSimilar(gameObject, exGameObject, true)
                        && gameObject.getStats().isSameStats(exGameObject.getStats()) && gameObject.getGuid() != exGameObject.getGuid()
                        && !Constant.isIncarnationWeapon(exGameObject.getTemplate().getId())
                        && exGameObject.getTemplate().getType() != Constant.ITEM_TYPE_CERTIFICAT_CHANIL
                        && exGameObject.getTemplate().getType() != Constant.ITEM_TYPE_PIERRE_AME_PLEINE
                        && gameObject.getTemplate().getType() != Constant.ITEM_TYPE_OBJET_ELEVAGE
                        && gameObject.getTemplate().getType() != Constant.ITEM_TYPE_CERTIF_MONTURE
                        && (exGameObject.getTemplate().getType() != Constant.ITEM_TYPE_QUETES || Constant.isFlacGelee(gameObject.getTemplate().getId()))
                        && !Constant.isCertificatDopeuls(gameObject.getTemplate().getId()) &&
                        gameObject.getTemplate().getType() != Constant.ITEM_TYPE_FAMILIER &&
                        gameObject.getTemplate().getType() != Constant.ITEM_TYPE_OBJET_VIVANT && gameObject.getPosition() == Constant.ITEM_POS_NO_EQUIPED)
                    return gameObject;
        }

        return null;
    }

    public int learnJob(Job m) {
        for (Entry<Integer, JobStat> entry : _metiers.entrySet()) {
            if (entry.getValue().getTemplate().getId() == m.getId())//Si le joueur a d�j� le m�tier
                return -1;
        }
        int Msize = _metiers.size();
        if (Msize == 6)//Si le joueur a d�j� 6 m�tiers
            return -1;
        int pos = 0;
        if (JobConstant.isMageJob(m.getId())) {
            if (_metiers.get(5) == null)
                pos = 5;
            if (_metiers.get(4) == null)
                pos = 4;
            if (_metiers.get(3) == null)
                pos = 3;
        } else {
            if (_metiers.get(2) == null)
                pos = 2;
            if (_metiers.get(1) == null)
                pos = 1;
            if (_metiers.get(0) == null)
                pos = 0;
        }

        JobStat sm = new JobStat(pos, m, 1, 0);
        _metiers.put(pos, sm);//On apprend le m�tier lvl 1 avec 0 xp
        if (isOnline) {
            //on cr�er la listes des JobStats a envoyer (Seulement celle ci)
            ArrayList<JobStat> list = new ArrayList<>();
            list.add(sm);

            SocketManager.GAME_SEND_Im_PACKET(this, "02;" + m.getId());
            //packet JS
            SocketManager.GAME_SEND_JS_PACKET(this, list);
            //packet JX
            SocketManager.GAME_SEND_JX_PACKET(this, list);
            //Packet JO (Job Option)
            SocketManager.GAME_SEND_JO_PACKET(this, list);

            GameObject obj = getObjetByPos(Constant.ITEM_POS_ARME);
            if (obj != null)
                if (sm.getTemplate().isValidTool(obj.getTemplate().getId()))
                    SocketManager.GAME_SEND_OT_PACKET(getAccount().getGameClient(), m.getId());
        }
        return pos;
    }

    public boolean unlearnJob(int jobID) {
        Optional<Integer> key = _metiers.entrySet().stream()
            .filter(e -> e.getValue().getTemplate().getId() == jobID)
            .findFirst()
            .map(Entry::getKey);

        if(!key.isPresent()) return false;
        _metiers.remove(key.get());

        DatabaseManager.get(PlayerData.class).update(this);
        if(isOnline) {
            SocketManager.GAME_SEND_STATS_PACKET(this);
            send("JR" + jobID);
        }
        return true;
    }

    public void unequipedObjet(GameObject o) {
        o.setPosition(Constant.ITEM_POS_NO_EQUIPED);
        ObjectTemplate oTpl = o.getTemplate();
        int idSetExObj = oTpl.getPanoId();
        if ((idSetExObj >= 81 && idSetExObj <= 92)
                || (idSetExObj >= 201 && idSetExObj <= 212)) {
            String[] stats = oTpl.getStrTemplate().split(",");
            for (String stat : stats) {
                String[] val = stat.split("#");
                String modifi = Integer.parseInt(val[0], 16) + ";"
                        + Integer.parseInt(val[1], 16) + ";0";
                SocketManager.SEND_SB_SPELL_BOOST(this, modifi);
                this.removeObjectClassSpell(Integer.parseInt(val[1], 16));
            }
            this.removeObjectClass(oTpl.getId());
        }
        SocketManager.GAME_SEND_OBJET_MOVE_PACKET(this, o);
        if (oTpl.getPanoId() > 0)
            SocketManager.GAME_SEND_OS_PACKET(this, oTpl.getPanoId());
    }

    public void verifEquiped() {
        if (this.getMorphMode())
            return;
        GameObject arme = this.getObjetByPos(Constant.ITEM_POS_ARME);
        GameObject bouclier = this.getObjetByPos(Constant.ITEM_POS_BOUCLIER);
        if (arme != null) {
            if (arme.getTemplate().isTwoHanded() && bouclier != null) {
                this.unequipedObjet(arme);
                SocketManager.GAME_SEND_Im_PACKET(this, "119|44");
            } else if (!arme.getTemplate().getConditions().equalsIgnoreCase("")
                    && !World.world.getConditionManager().validConditions(this, arme.getTemplate().getConditions())) {
                this.unequipedObjet(arme);
                SocketManager.GAME_SEND_Im_PACKET(this, "119|44");
            }
        }
        if (bouclier != null) {
            /*if (!bouclier.getTemplate().getConditions().equalsIgnoreCase("")
                    && !World.world.getConditionManager().validConditions(this, bouclier.getTemplate().getConditions())) {
                this.unequipedObjet(bouclier);
                SocketManager.GAME_SEND_Im_PACKET(this, "119|44");
            }*/
        }
    }

    public boolean hasEquiped(int id) {
        for (GameObject object : objects.values())
            if (object.getTemplate() != null && object.getTemplate().getId() == id && object.getPosition() != Constant.ITEM_POS_NO_EQUIPED)
                return true;
        return false;
    }

    public int getInvitation() {
        return _inviting;
    }

    public void setInvitation(int target) {
        _inviting = target;
    }

    public String parseToPM() {
        StringBuilder str = new StringBuilder();
        str.append(this.getId()).append(";");
        str.append(this.getName()).append(";");
        str.append(gfxId).append(";");
        int color1 = this.getColor1(), color2 = this.getColor2(), color3 = this.getColor3();
        if (this.getObjetByPos(Constant.ITEM_POS_MALEDICTION) != null)
            if (this.getObjetByPos(Constant.ITEM_POS_MALEDICTION).getTemplate().getId() == 10838) {
                color1 = 16342021;
                color2 = 16342021;
                color3 = 16342021;
            }
        str.append(color1).append(";");
        str.append(color2).append(";");
        str.append(color3).append(";");
        str.append(getGMStuffString()).append(";");
        str.append(this.curPdv).append(",").append(this.maxPdv).append(";");
        str.append(this.getLevel()).append(";");
        str.append(getInitiative()).append(";");
        str.append(getTotalStats(false).getEffect(Constant.STATS_ADD_PROS)
                + ((int) Math.ceil(getTotalStats(false).getEffect(Constant.STATS_ADD_CHAN) / 10))).append(";");
        str.append("0");//Side = ?
        return str.toString();
    }

    public int getNumbEquipedItemOfPanoplie(int panID) {
        int nb = 0;

        for (Entry<Integer, GameObject> i : objects.entrySet()) {
            //On ignore les objets non �quip�s
            if (i.getValue().getPosition() == Constant.ITEM_POS_NO_EQUIPED)
                continue;
            //On prend que les items de la pano demand�e, puis on augmente le nombre si besoin
            if (i.getValue().getTemplate().getPanoId() == panID)
                nb++;
        }
        return nb;
    }

    public void startActionOnCell(GameAction GA) {
        int cellID = -1;
        int action = -1;
        try {
            cellID = Integer.parseInt(GA.args.split(";")[0]);
            action = Integer.parseInt(GA.args.split(";")[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cellID == -1 || action == -1)
            return;

        // Make sure this skill is available on this cell
        InteractiveObject io = curMap.getInteractiveObject(cellID);


        DataScriptVM.getInstance().handlers.onSkillUse(this, cellID, action);
    }

    public void finishActionOnCell(GameAction GA) {
        int cellID = -1;
        try {
            cellID = Integer.parseInt(GA.args.split(";")[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cellID == -1 || this.curMap == null)
            return;
        GameCase cell = this.curMap.getCase(cellID);
        if(cell == null) return;

        // TODO: Call Lua to finish gathering skills

        // curMap.finishAction(cell,)
        // cell.finishAction(this, GA);
    }

    public void teleportD(int newMapID, int newCellID) {
        if (this.getFight() != null) return;
        this.curMap = World.world.getMap(newMapID);
        this.curCell = World.world.getMap(newMapID).getCase(newCellID);
        ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
    }

    public void teleportLaby(short newMapID, int newCellID) {
        if (this.getFight() != null) return;
        GameClient client = this.getGameClient();
        if (client == null)
            return;

        if (World.world.getMap(newMapID) == null)
            return;

        if (World.world.getMap(newMapID).getCase(newCellID) == null)
            return;

        SocketManager.GAME_SEND_GA2_PACKET(client, this.getId());
        SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.curMap, this.getId());

        if (this.getMount() != null)
            if (this.getMount().getFatigue() >= 220)
                this.getMount().setEnergy(this.getMount().getEnergy() - 1);

        if (this.curCell.getPlayers().contains(this))
            this.curCell.removePlayer(this);
        this.curMap = World.world.getMap(newMapID);
        this.curCell = this.curMap.getCase(newCellID);

        SocketManager.GAME_SEND_MAPDATA(client, newMapID, this.curMap.getDate(), this.curMap.getKey());
        this.curMap.addPlayer(this);

        if (!this.follower.isEmpty())// On met a jour la Map des personnages qui nous suivent
        {
            for (Player t : this.follower.values()) {
                if (t.isOnline())
                    SocketManager.GAME_SEND_FLAG_PACKET(t, this);
                else
                    this.follower.remove(t.getId());
            }
        }
    }

    public void teleport(Pair<Integer,Integer> posIDs) {
        teleport(posIDs.first, posIDs.second);
    }

    public void teleport(int newMapID, int newCellID) {
        teleport(newMapID, newCellID, false);
    }

    public void teleport(int newMapID, int newCellID, boolean forceGDM) {
        GameClient client = this.getGameClient();
        GameMap map = World.world.getMap(newMapID);

        if (map == null || this.getFight() != null)
            return;
        if (map.getCase(newCellID) == null)
            return;

        if (!forceGDM && client != null && newMapID == this.curMap.getId()) {
            SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.curMap, this.getId());
            this.curCell.removePlayer(this);
            this.curCell = curMap.getCase(newCellID);
            this.curMap.addPlayer(this);
            SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(this.curMap, this);
            return;
        }

        this.setAway(false);
        boolean fullmorph = false;
        if (Constant.isInMorphDonjon(this.curMap.getId()))
            if (!Constant.isInMorphDonjon(newMapID))
                fullmorph = true;

        if(client != null)
            SocketManager.GAME_SEND_GA2_PACKET(client, this.getId());
        SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.curMap, this.getId());

        if (this.getMount() != null)
            if (this.getMount().getFatigue() >= 220)
                this.getMount().setEnergy(this.getMount().getEnergy() - 1);

        if (this.curCell.getPlayers().contains(this))
            this.curCell.removePlayer(this);

        this.curMap = map;
        this.curCell = this.curMap.getCase(newCellID);
        // Verification de la Map
        // Verifier la validit� du mountpark

        if (this.curMap.getMountPark() != null
                && this.curMap.getMountPark().getOwner() > 0
                && this.curMap.getMountPark().getGuild().getId() != -1) {
            if (World.world.getGuild(this.curMap.getMountPark().getGuild().getId()) == null) {// Ne devrait  pas  arriver
                GameServer.a();
                //FIXME : Map.MountPark.removeMountPark(curMap.getMountPark().getGuild().getId());
            }
        }

        Collector collector = Collector.getCollectorByMapId(this.curMap.getId());
        if (collector != null && World.world.getGuild(collector.getGuildId()) == null)
            Collector.removeCollector(collector.getGuildId());

        if (this.isMissingSubscription()) {
            if (!this.isInPrivateArea)
                SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this.getGameClient(), 'S');
            this.isInPrivateArea = true;
        } else {
            this.isInPrivateArea = false;
        }

        if(client != null) {
            SocketManager.GAME_SEND_MAPDATA(client, newMapID, this.curMap.getDate(), this.curMap.getKey());
            this.curMap.addPlayer(this);
        }

        if (fullmorph)
            this.unsetFullMorph();

        if (this.follower != null && !this.follower.isEmpty())// On met a jour la Map des personnages qui nous suivent
        {
            for (Player t : this.follower.values()) {
                if (t.isOnline())
                    SocketManager.GAME_SEND_FLAG_PACKET(t, this);
                else
                    this.follower.remove(t.getId());
            }
        }

        if (this.getInHouse() != null) {
            if (this.getInHouse().getMapId() == this.curMap.getId()) {
                this.setInHouse(null);
            }
        }

        // We changed map. Call event handler
        DataScriptVM.getInstance().handlers.onMapEnter(this);
    }

    public void teleport(GameMap map, int cell) {
        if (this.getFight() != null) return;
        GameClient PW = null;
        if (getAccount().getGameClient() != null)
            PW = getAccount().getGameClient();
        if (map == null)
            return;
        if (map.getCase(cell) == null)
            return;
        if (!cantTP()) {
            if (this.getCurMap().getSubArea() != null
                    && map.getSubArea() != null) {
                if (this.getCurMap().getSubArea().getId() == 165
                        && map.getSubArea().getId() == 165) {
                    if (this.hasItemTemplate(997, 1, false)) {
                        this.removeItemByTemplateId(997, 1, false);
                    } else {
                        SocketManager.GAME_SEND_Im_PACKET(this, "14");
                        return;
                    }
                }
            }
        }

        boolean fullmorph = false;
        if (Constant.isInMorphDonjon(curMap.getId()))
            if (!Constant.isInMorphDonjon(map.getId()))
                fullmorph = true;

        if (map.getId() == curMap.getId()) {
            SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(curMap, this.getId());
            curCell.removePlayer(this);
            curCell = curMap.getCase(cell);
            curMap.addPlayer(this);
            SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(curMap, this);
            if (fullmorph)
                this.unsetFullMorph();
            return;
        }
        if (PW != null) {
            SocketManager.GAME_SEND_GA2_PACKET(PW, this.getId());
            SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(curMap, this.getId());
        }
        if (this.getMount() != null)
            if (this.getMount().getFatigue() >= 220)
                this.getMount().setEnergy(this.getMount().getEnergy() - 1);
        curCell.removePlayer(this);
        curMap = map;
        curCell = curMap.getCase(cell);
        // Verification de la Map
        // Verifier la validit� du Collector
        if (Collector.getCollectorByMapId(curMap.getId()) != null) {
            if (World.world.getGuild(Collector.getCollectorByMapId(curMap.getId()).getGuildId()) == null)// Ne devrait pas arriver
            {
                GameServer.a();
                Collector.removeCollector(Collector.getCollectorByMapId(curMap.getId()).getGuildId());
            }
        }

        if (PW != null) {
            SocketManager.GAME_SEND_MAPDATA(PW, map.getId(), curMap.getDate(), curMap.getKey());
            curMap.addPlayer(this);
            if (fullmorph)
                this.unsetFullMorph();
        }

        if (!follower.isEmpty())// On met a jour la Map des personnages qui nous suivent
        {
            for (Player t : follower.values()) {
                if (t.isOnline())
                    SocketManager.GAME_SEND_FLAG_PACKET(t, this);
                else
                    follower.remove(t.getId());
            }
        }
    }

    public void disconnectInFight() {
        //Si en groupe
        if (getParty() != null)
            getParty().leave(this);
        resetVars();
        ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
        set_isClone(true);
        World.world.unloadPerso(this.getId());
    }

    public int getBankCost() {
        return getAccount().getBank().size();
    }

    public void openBank() {
        if(this.getExchangeAction().getType() == ExchangeAction.TALKING_WITH) {
            NpcDialogActionData data = (NpcDialogActionData) this.getExchangeAction().getValue();

            if(!data.getNpcTemplate().isBankClerk()) {
                // Opening bank while talking to an NPC is not valid, except when the NPc is a bank clerk
                return;
            }
            // We were talking to a clerk, close dialog
            this.exchangeAction = null;
            SocketManager.GAME_SEND_END_DIALOG_PACKET(this.getGameClient());
        }
        if(this.getExchangeAction() != null) {
            return;
        }
        if (this.getDeshonor() >= 1) {
            SocketManager.GAME_SEND_Im_PACKET(this, "183");
            return;
        }

        final int cost = this.getBankCost();
        ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
        if (cost > 0) {

            final long kamas = this.getKamas();
            final long remaining = kamas - cost;
            final long bank = this.getAccount().getBankKamas();
            final long total = bank + kamas;
            if (remaining < 0) {

                if (bank >= cost) {

                    this.setBankKamas(bank - cost);
                } else if (total >= cost) {

                    this.setKamas(0);
                    this.setBankKamas(total - cost);
                    SocketManager.GAME_SEND_STATS_PACKET(this);
                    SocketManager.GAME_SEND_Im_PACKET(this, "020;" + kamas);
                } else {

                    SocketManager.GAME_SEND_MESSAGE_SERVER(this, "10|" + cost);
                    return;
                }
            } else {

                this.setKamas(remaining);
                SocketManager.GAME_SEND_STATS_PACKET(this);
                SocketManager.GAME_SEND_Im_PACKET(this, "020;" + cost);
            }
        }
        SocketManager.GAME_SEND_ECK_PACKET(this.getGameClient(), 5, "");
        SocketManager.GAME_SEND_EL_BANK_PACKET(this);
        this.setAway(true);
        this.setExchangeAction(new ExchangeAction<>(ExchangeAction.IN_BANK, 0));

    }

    public String getStringVar(String str) {
        switch (str) {
            case "[name]":
                return this.getName();
            case "[bankCost]":
                return getBankCost() + "";
            case "[points]":
                return this.getAccount().getPoints() + "";
            case "[nbrOnline]":
                return Config.gameServer.getClients().size() + "";
            case "[align]":
                return World.world.getStatOfAlign();
            default:
                return str;
        }
    }

    public void refreshMapAfterFight() {
        SocketManager.send(this, "ILS" + 2000);
        this.regenRate = 2000;
        this.curMap.addPlayer(this);
        if (getAccount().getGameClient() != null)
            SocketManager.GAME_SEND_STATS_PACKET(this);
        this.fight = null;
        this.away = false;
    }

    public long getBankKamas() {
        return getAccount().getBankKamas();
    }

    public void setBankKamas(long i) {
        Account account = getAccount();
        account.setBankKamas(i);
        ((BankData) DatabaseManager.get(BankData.class)).update(account);
    }

    public String parseBankPacket() {
        StringBuilder packet = new StringBuilder();
        for (GameObject entry : getAccount().getBank())
            packet.append("O").append(entry.encodeItem()).append(";");
        if (getBankKamas() != 0)
            packet.append("G").append(getBankKamas());
        return packet.toString();
    }

    public void addCapital(int pts) {
        _capital += pts;
    }

    public void addSpellPoint(int pts) {
        if (_morphMode)
            _saveSpellPts += pts;
        else
            _spellPts += pts;
    }

    public void addInBank(int guid, int qua, boolean outside) {
        if (qua <= 0)
            return;
        GameObject PersoObj = World.world.getGameObject(guid);

        if (!outside && this.objects == null) return;

        if (!outside && objects.get(guid) == null) // Si le joueur n'a pas l'item dans son sac ...
            return;

        if (PersoObj == null || PersoObj.getPosition() != Constant.ITEM_POS_NO_EQUIPED) // Si c'est un item �quip� ...
            return;

        Account account = getAccount();
        GameObject BankObj = getSimilarBankItem(PersoObj);
        int newQua = PersoObj.getQuantity() - qua;
        if (BankObj == null) // Ajout d'un nouvel objet dans la banque
        {
            if (newQua <= 0) // Ajout de toute la quantit� disponible
            {
                removeItem(PersoObj.getGuid()); // On enleve l'objet du sac du joueur
                account.getBank().add(PersoObj); // On met l'objet du sac dans la banque, avec la meme quantit�
                String str = "O+" + PersoObj.getGuid() + "|"
                        + PersoObj.getQuantity() + "|"
                        + PersoObj.getTemplate().getId() + "|"
                        + PersoObj.encodeStats();
                SocketManager.GAME_SEND_EsK_PACKET(this, str);
                SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
            } else
            //S'il reste des objets au joueur
            {
                PersoObj.setQuantity(newQua); //on modifie la quantit� d'item du sac
                BankObj = PersoObj.getClone(qua, true); //On ajoute l'objet a la banque et au monde
                World.world.addGameObject(BankObj);
                account.getBank().add(BankObj);

                String str = "O+" + BankObj.getGuid() + "|"
                        + BankObj.getQuantity() + "|"
                        + BankObj.getTemplate().getId() + "|"
                        + BankObj.encodeStats();
                SocketManager.GAME_SEND_EsK_PACKET(this, str); //Envoie des packets
                SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
            }
        } else
        // S'il y avait un item du meme template
        {
            if (newQua <= 0) //S'il ne reste pas d'item dans le sac
            {
                removeItem(PersoObj.getGuid()); //On enleve l'objet du sac du joueur
                World.world.removeGameObject(PersoObj.getGuid()); //On enleve l'objet du monde
                BankObj.setQuantity(BankObj.getQuantity()
                        + PersoObj.getQuantity()); //On ajoute la quantit� a l'objet en banque
                String str = "O+" + BankObj.getGuid() + "|"
                        + BankObj.getQuantity() + "|"
                        + BankObj.getTemplate().getId() + "|"
                        + BankObj.encodeStats(); //on envoie l'ajout a la banque de l'objet
                SocketManager.GAME_SEND_EsK_PACKET(this, str);
                SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, guid); //on envoie la supression de l'objet du sac au joueur
            } else
            //S'il restait des objets
            {
                PersoObj.setQuantity(newQua); //on modifie la quantit� d'item du sac
                BankObj.setQuantity(BankObj.getQuantity() + qua);
                String str = "O+" + BankObj.getGuid() + "|"
                        + BankObj.getQuantity() + "|"
                        + BankObj.getTemplate().getId() + "|"
                        + BankObj.encodeStats();
                SocketManager.GAME_SEND_EsK_PACKET(this, str);
                SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
            }
        }
        SocketManager.GAME_SEND_Ow_PACKET(this);
        ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
        ((BankData) DatabaseManager.get(BankData.class)).update(getAccount());
    }

    private GameObject getSimilarBankItem(GameObject exGameObject) {
        Account account = getAccount();
        if(account.getBank() != null)
            for (GameObject gameObject : account.getBank())
                if (gameObject != null && World.world.getConditionManager().stackIfSimilar(gameObject, exGameObject, true))
                    return gameObject;
        return null;
    }

    public void removeFromBank(int guid, int qua) {
        if (qua <= 0)
            return;
        GameObject BankObj = World.world.getGameObject(guid);

        //Si le joueur n'a pas l'item dans sa banque ...
        int index = getAccount().getBank().indexOf(BankObj);
        if (index == -1)
            return;

        GameObject PersoObj = getSimilarItem(BankObj);
        int newQua = BankObj.getQuantity() - qua;

        if (PersoObj == null)//Si le joueur n'avait aucun item similaire
        {
            //S'il ne reste rien en banque
            if (newQua <= 0) {
                //On retire l'item de la banque
                getAccount().getBank().remove(index);
                //On l'ajoute au joueur

                objects.put(guid, BankObj);


                //On envoie les packets
                SocketManager.GAME_SEND_OAKO_PACKET(this, BankObj);
                String str = "O-" + guid;
                SocketManager.GAME_SEND_EsK_PACKET(this, str);
            } else
            //S'il reste des objets en banque
            {
                //On cr�e une copy de l'item en banque
                PersoObj = BankObj.getClone(qua, true);
                //On l'ajoute au monde
                World.world.addGameObject(PersoObj);
                //On retire X objet de la banque
                BankObj.setQuantity(newQua);
                //On l'ajoute au joueur

                objects.put(PersoObj.getGuid(), PersoObj);


                //On envoie les packets
                SocketManager.GAME_SEND_OAKO_PACKET(this, PersoObj);
                String str = "O+" + BankObj.getGuid() + "|"
                        + BankObj.getQuantity() + "|"
                        + BankObj.getTemplate().getId() + "|"
                        + BankObj.encodeStats();
                SocketManager.GAME_SEND_EsK_PACKET(this, str);
            }
        } else {
            //S'il ne reste rien en banque
            if (newQua <= 0) {
                //On retire l'item de la banque
                getAccount().getBank().remove(index);
                World.world.removeGameObject(BankObj.getGuid());
                //On Modifie la quantit� de l'item du sac du joueur
                PersoObj.setQuantity(PersoObj.getQuantity()
                        + BankObj.getQuantity());

                //On envoie les packets
                SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
                String str = "O-" + guid;
                SocketManager.GAME_SEND_EsK_PACKET(this, str);
            } else
            //S'il reste des objets en banque
            {
                //On retire X objet de la banque
                BankObj.setQuantity(newQua);
                //On ajoute X objets au joueurs
                PersoObj.setQuantity(PersoObj.getQuantity() + qua);

                //On envoie les packets
                SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
                String str = "O+" + BankObj.getGuid() + "|"
                        + BankObj.getQuantity() + "|"
                        + BankObj.getTemplate().getId() + "|"
                        + BankObj.encodeStats();
                SocketManager.GAME_SEND_EsK_PACKET(this, str);
            }
        }

        SocketManager.GAME_SEND_Ow_PACKET(this);

        ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
        ((BankData) DatabaseManager.get(BankData.class)).update(getAccount());
    }

    /**
     * MountPark *
     * @param target
     */
    public void openMountPark(MountPark target) {
        if (this.getDeshonor() >= 5) {
            SocketManager.GAME_SEND_Im_PACKET(this, "183");
            return;
        }

        final MountPark park = target == null ? this.curMap.getMountPark() : target;

        if (this.getGuildMember() != null && park.getGuild() != null) {
            if (park.getGuild().getId() == this.getGuildMember().getGuild().getId()) {
                if (!this.getGuildMember().canDo(Constant.G_USEENCLOS)) {
                    SocketManager.GAME_SEND_Im_PACKET(this, "1101");
                    return;
                }
            }
        }

        this.setExchangeAction(new ExchangeAction<>(ExchangeAction.IN_MOUNTPARK, park));
        this.away = true;

        StringBuilder packet = new StringBuilder();

        if (park.getEtable().size() > 0) {
            for (Mount mount : park.getEtable()) {
                if (mount == null || mount.getSize() == 50) continue;
                if (!packet.toString().isEmpty()) packet.append(";");
                if (mount.getOwner() == this.getId()) packet.append(mount.parse());
            }
        }

        packet.append("~");

        if (park.getListOfRaising().size() > 0) {
            boolean first1 = false;
            for (Integer id : park.getListOfRaising()) {
                Mount mount = World.world.getMountById(id);
                if (mount == null) continue;

                if (mount.getOwner() == this.getId()) {
                    if (first1)
                        packet.append(";");
                    packet.append(mount.parse());
                    first1 = true;
                    continue;
                }
                if (getGuildMember() != null) {
                    if (getGuildMember().canDo(Constant.G_OTHDINDE) && park.getOwner() != -1 && park.getGuild() != null) {
                        if (park.getGuild().getId() == this.getGuild().getId()) {
                            if (first1) packet.append(";");
                            packet.append(mount.parse());
                            first1 = true;
                        }
                    }
                }
            }
        }

        SocketManager.GAME_SEND_ECK_PACKET(this, 16, packet.toString());
        TimerWaiter.addNext(() -> park.getEtable().stream().filter(mount -> mount != null && mount.getSize() == 50 && mount.getOwner() == this.getId()).forEach(mount -> SocketManager.GAME_SEND_Ee_PACKET_WAIT(this, '~', mount.parse())), 500);
    }

    public void fullPDV() {
        this.setPdv(this.getMaxPdv());
        SocketManager.GAME_SEND_STATS_PACKET(this);
    }

    public void warpToSavePos() {
        try {
            this.teleport(this._savePos.first, this._savePos.second, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean removeItemByTemplateId(int templateId, int count, boolean display) {
        // TODO: Rewrite this function to be fail-safe
        // Currently, if we try to remove 10 items but the user only has 9, it removes 9 items then fails.
        ArrayList<GameObject> remove = new ArrayList<>();
        int tempCount = count;

        //on verifie pour chaque objet
        for (GameObject item : new ArrayList<>(objects.values())) {
            //Si mauvais TemplateID, on passe
            if (item.getTemplate().getId() != templateId)
                continue;

            if (item.getQuantity() >= count) {
                int newQua = item.getQuantity() - count;
                if (newQua > 0) {
                    item.setQuantity(newQua);
                    if (isOnline) SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, item);
                } else {
                    //on supprime de l'inventaire et du Monde
                    objects.remove(item.getGuid());
                    World.world.removeGameObject(item.getGuid());
                    //on envoie le packet si connect�
                    if (isOnline) SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, item.getGuid());
                }
                if (isOnline) {
                    if (display) {
                        SocketManager.GAME_SEND_Im_PACKET(this, "022;" + count + "~" + item.getTemplate().getId());
                    }
                    SocketManager.GAME_SEND_Ow_PACKET(this);
                }
                return true;
            } else {
                //Si pas assez d'objet
                if (item.getQuantity() >= tempCount) {
                    int newQua = item.getQuantity() - tempCount;
                    if (newQua > 0) {
                        item.setQuantity(newQua);
                        if (isOnline) SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, item);
                    } else {
                        remove.add(item);
                    }

                    for (GameObject o : remove) {
                        //on supprime de l'inventaire et du Monde

                        objects.remove(o.getGuid());

                        World.world.removeGameObject(o.getGuid());
                        //on envoie le packet si connect�
                        if (isOnline) SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, o.getGuid());
                    }
                    if (isOnline) {
                        if(display) {
                            SocketManager.GAME_SEND_Im_PACKET(this, "022;" + count + "~" + item.getTemplate().getId());
                        }
                        SocketManager.GAME_SEND_Ow_PACKET(this);
                    }
                    return true;
                } else {
                    // on r�duit le compteur
                    tempCount -= item.getQuantity();
                    remove.add(item);
                }
            }
        }
        // We failed
        return false;
    }

    public List<Job> getJobs() {
        return Collections.unmodifiableList(_metiers.values().stream().map(JobStat::getTemplate).collect(Collectors.toList()));
    }

    public Map<Integer, JobStat> getMetiers() {
        return _metiers;
    }

    public void doJobAction(int skillId, int actionId, int cellId, InteractiveObject io) {
        JobStat SM = getMetierBySkill(skillId);
        if (SM == null) {
            switch (skillId) {
                case 151:
                    new JobAction(151, 4, 0, true, 100, 0).startAction(this);
                    return;
                case 121:
                    new JobAction(121, 8, 0, true, 100, 0).startAction(this);
                    return;
                case 110:
                    new JobAction(110, 2, 0, true, 100, 0).startAction(this);
                    return;
                case 22:
                    new JobAction(22, 1, 0, true, 100, 0).startAction(this);
                    return;
                default:
                    return;
            }
        } else {
            SM.startAction(skillId, this, actionId, cellId, io);
        }
        SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(curMap, cellId, io);
    }

    public void finishJobAction(int actionID, InteractiveObject object,
                                GameAction GA, int cellId) {
        JobStat SM = getMetierBySkill(actionID);
        if (SM == null)
            return;
        SM.endAction(this, object, GA, cellId);
    }

    public String parseJobData() {
        StringBuilder str = new StringBuilder();
        if (_metiers.isEmpty())
            return "";
        for (JobStat SM : _metiers.values()) {
            if (SM == null)
                continue;
            if (str.length() > 0)
                str.append(";");
            str.append(SM.getTemplate().getId()).append(",").append(SM.getXp());
        }
        return str.toString();
    }

    public int totalJobBasic() {
        int i = 0;

        for (JobStat SM : _metiers.values()) {
            // Si c'est un m�tier 'basic' :
            if (SM.getTemplate().getId() == 2 || SM.getTemplate().getId() == 11
                    || SM.getTemplate().getId() == 13
                    || SM.getTemplate().getId() == 14
                    || SM.getTemplate().getId() == 15
                    || SM.getTemplate().getId() == 16
                    || SM.getTemplate().getId() == 17
                    || SM.getTemplate().getId() == 18
                    || SM.getTemplate().getId() == 19
                    || SM.getTemplate().getId() == 20
                    || SM.getTemplate().getId() == 24
                    || SM.getTemplate().getId() == 25
                    || SM.getTemplate().getId() == 26
                    || SM.getTemplate().getId() == 27
                    || SM.getTemplate().getId() == 28
                    || SM.getTemplate().getId() == 31
                    || SM.getTemplate().getId() == 36
                    || SM.getTemplate().getId() == 41
                    || SM.getTemplate().getId() == 56
                    || SM.getTemplate().getId() == 58
                    || SM.getTemplate().getId() == 60
                    || SM.getTemplate().getId() == 65) {
                i++;
            }
        }
        return i;
    }

    public int totalJobFM() {
        int i = 0;

        for (JobStat SM : _metiers.values()) {
            // Si c'est une sp�cialisation 'FM' :
            if (SM.getTemplate().getId() == 43
                    || SM.getTemplate().getId() == 44
                    || SM.getTemplate().getId() == 45
                    || SM.getTemplate().getId() == 46
                    || SM.getTemplate().getId() == 47
                    || SM.getTemplate().getId() == 48
                    || SM.getTemplate().getId() == 49
                    || SM.getTemplate().getId() == 50
                    || SM.getTemplate().getId() == 62
                    || SM.getTemplate().getId() == 63
                    || SM.getTemplate().getId() == 64) {
                i++;
            }
        }
        return i;
    }

    public boolean canAggro() {
        return canAggro;
    }

    public void setCanAggro(boolean canAggro) {
        this.canAggro = canAggro;
    }

    public JobStat getMetierBySkill(int skID) {
        for (JobStat SM : _metiers.values())
            if (SM.isValidMapAction(skID))
                return SM;
        return null;
    }

    public String parseToFriendList(int guid) {
        StringBuilder str = new StringBuilder();
        str.append(";");
        str.append("?;");
        str.append(this.getName()).append(";");
        if (getAccount().isFriendWith(guid)) {
            str.append(this.getLevel()).append(";");
            str.append(alignment).append(";");
        } else {
            str.append("?;");
            str.append("-1;");
        }
        str.append(this.getClasse()).append(";");
        str.append(this.getSexe()).append(";");
        str.append(gfxId);
        return str.toString();
    }

    public String parseToEnemyList(int guid) {
        StringBuilder str = new StringBuilder();
        str.append(";");
        str.append("?;");
        str.append(this.getName()).append(";");
        if (getAccount().isFriendWith(guid)) {
            str.append(this.getLevel()).append(";");
            str.append(alignment).append(";");
        } else {
            str.append("?;");
            str.append("-1;");
        }
        str.append(this.getClasse()).append(";");
        str.append(this.getSexe()).append(";");
        str.append(gfxId);
        return str.toString();
    }

    public JobStat getMetierByID(int job) {
        for (JobStat SM : _metiers.values())
            if (SM.getTemplate().getId() == job)
                return SM;
        return null;
    }

    public boolean isOnMount() {
        return _onMount;
    }

    public void toogleOnMount() {
        if (_mount == null || this.isMorph() || this.getLevel() < 60)
            return;
        if (Config.subscription) {
            SocketManager.GAME_SEND_Im_PACKET(this, "1115");
            return;
        }
        if (this.getClasse() * 10 + this.getSexe() != this.getGfxId())
            return;
        if (this.getInHouse() != null) {
            SocketManager.GAME_SEND_Im_PACKET(this, "1117");
            return;
        }
        if (!_onMount && _mount.isMontable() == 0) {
            SocketManager.GAME_SEND_Re_PACKET(this, "Er", null);
            return;
        }

        if (_mount.getEnergy() < Formulas.calculEnergieLooseForToogleMount(_mount.getFatigue())) {
            SocketManager.GAME_SEND_Im_PACKET(this, "1113");
            return;
        }

        if (!_onMount) {
            int EnergyoLose = _mount.getEnergy()
                    - Formulas.calculEnergieLooseForToogleMount(_mount.getFatigue());
            _mount.setEnergy(EnergyoLose);
        }

        _onMount = !_onMount;
        GameObject obj = getObjetByPos(Constant.ITEM_POS_FAMILIER);

        if (_onMount && obj != null) {
            obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
            SocketManager.GAME_SEND_OBJET_MOVE_PACKET(this, obj);
        }

        if (_mount.getEnergy() <= 0) {
            _mount.setEnergy(0);
            SocketManager.GAME_SEND_Im_PACKET(this, "1114");
            return;
        }
        //on envoie les packets
        if (getFight() != null && getFight().getState() == 2) {
            SocketManager.GAME_SEND_ALTER_FIGHTER_MOUNT(getFight(), getFight().getFighterByPerso(this), getId(), getFight().getTeamId(getId()), getFight().getOtherTeamId(getId()));
        } else {
            SocketManager.GAME_SEND_ALTER_GM_PACKET(curMap, this);
        }
        SocketManager.GAME_SEND_Re_PACKET(this, "+", _mount);
        SocketManager.GAME_SEND_Rr_PACKET(this, _onMount ? "+" : "-");
        SocketManager.GAME_SEND_STATS_PACKET(this);

    }

    public int getMountXpGive() {
        return _mountXpGive;
    }

    public Mount getMount() {
        return _mount;
    }

    public void setMount(Mount DD) {
        _mount = DD;
    }

    public void setMountGiveXp(int parseInt) {
        _mountXpGive = parseInt;
    }

    public void resetVars() {
        if (this.getExchangeAction() != null) {
            if (this.getExchangeAction().getValue() instanceof JobAction && ((JobAction) this.getExchangeAction().getValue()).getJobCraft() != null)
                ((JobAction) this.getExchangeAction().getValue()).getJobCraft().getJobAction().broke = true;
            this.setExchangeAction(null);
        }

        doAction = false;
        this.setGameAction(null);

        away = false;
        _emoteActive = 0;
        fight = null;
        duelId = 0;
        ready = false;
        party = null;
        _inviting = 0;
        sitted = false;
        _onMount = false;
        _isClone = false;
        _isAbsent = false;
        _isInvisible = false;
        follower.clear();
        follow = null;
        _curHouse = null;
        isGhost = false;
        _livreArti = false;
        _spec = false;
        afterFight = false;
    }

    public void addChanel(String chan) {
        if (_canaux.indexOf(chan) >= 0)
            return;
        _canaux += chan;
        SocketManager.GAME_SEND_cC_PACKET(this, '+', chan);
    }

    public void removeChanel(String chan) {
        _canaux = _canaux.replace(chan, "");
        SocketManager.GAME_SEND_cC_PACKET(this, '-', chan);
    }

    public void modifAlignement(int i) {
        _honor = 0;
        _deshonor = 0;
        alignment = (byte) i;
        _aLvl = 1;
        SocketManager.GAME_SEND_ZC_PACKET(this, i);
        SocketManager.GAME_SEND_STATS_PACKET(this);
        toggleWings('+');
    }

    public int getDeshonor() {
        return _deshonor;
    }

    public void setDeshonor(int deshonor) {
        _deshonor = deshonor;
    }

    public void setShowWings(boolean showWings) {
        _showWings = showWings;
    }

    public int get_honor() {
        return _honor;
    }

    public void set_honor(int honor) {
        _honor = honor;
    }

    public int getALvl() {
        return _aLvl;
    }

    public void setALvl(int a) {
        _aLvl = a;
    }

    public void toggleWings(char type) {
        if (this.alignment == Constant.ALIGNEMENT_NEUTRE || Config.modeHeroic) {
            this.send("BN");
            return;
        }

        int loose = this._honor * 5 / 100;
        switch (type) {
            case '*':
                SocketManager.GAME_SEND_GIP_PACKET(this, loose);
                return;
            case '+':
                this.setShowWings(true);
                SocketManager.GAME_SEND_ALTER_GM_PACKET(this.curMap, this);
                ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
                break;
            case '-':
                this.setShowWings(false);
                this._honor -= loose;
                SocketManager.GAME_SEND_ALTER_GM_PACKET(this.curMap, this);
                ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
                break;
        }
        SocketManager.GAME_SEND_STATS_PACKET(this);
    }

    public void addHonor(int winH) {
        if (alignment == 0)
            return;
        int curGrade = getGrade();
        _honor += winH;
        if (_honor > 18000) _honor = 18000;
        SocketManager.GAME_SEND_Im_PACKET(this, "080;" + winH);
        //Changement de grade
        if (getGrade() != curGrade) {
            SocketManager.GAME_SEND_Im_PACKET(this, "082;" + getGrade());
        }
    }

    public void remHonor(int losePH) {
        if (alignment == 0)
            return;
        int curGrade = getGrade();
        _honor -= losePH;
        SocketManager.GAME_SEND_Im_PACKET(this, "081;" + losePH);
        //Changement de grade
        if (getGrade() != curGrade) {
            SocketManager.GAME_SEND_Im_PACKET(this, "083;" + getGrade());
        }
    }

    public GuildMember getGuildMember() {
        return _guildMember;
    }

    public void setGuildMember(GuildMember _guild) {
        this._guildMember = _guild;
    }

    public int getAccID() {
        return _accID;
    }

    public String parseZaapList()//Pour le packet WC
    {
        int map = Optional.ofNullable(_savePos).map(p -> p.first).orElse(curMap.getId());

        StringBuilder str = new StringBuilder();
        str.append(map);

        if(this.curMap.getSubArea() != null) {
            int superAreaID = curMap.getSubArea().getArea().getSuperArea();
            for (int i : _zaaps) {
                try {
                    if (World.world.getMap(i) == null)
                        continue;
                }catch(NullPointerException e) {
                    Main.logger.error("Unknown zaap map #{}", i);
                    continue;
                }
                if (World.world.getMap(i).getSubArea().getArea().getSuperArea() != superAreaID)
                    continue;
                int cost = Formulas.calculZaapCost(this, curMap, World.world.getMap(i));
                if (i == curMap.getId())
                    cost = 0;
                str.append("|").append(i).append(";").append(cost);
            }
        }
        return str.toString();
    }

    public String parsePrismesList() {
        String map = curMap.getId() + "";
        String str = map + "";
        for (Prism Prisme : World.world.AllPrisme()) {
            if (Prisme.getAlignment() != alignment)
                continue;
            int MapID = Prisme.getMap();
            if (World.world.getMap(MapID) == null)
                continue;
            if (Prisme.getFight() != null) {
                str += "|" + MapID + ";*";
            } else {
                int costo = Formulas.calculZaapCost(this, curMap, World.world.getMap(MapID));
                if (MapID == curMap.getId())
                    costo = 0;
                str += "|" + MapID + ";" + costo;
            }
        }
        return str;
    }

    public void openZaapMenu() {
        if (this.fight == null) {
            if (!verifOtomaiZaap())
                return;
            if (getDeshonor() >= 3) {
                SocketManager.GAME_SEND_Im_PACKET(this, "183");
                return;
            }

            this.setExchangeAction(new ExchangeAction<>(ExchangeAction.IN_ZAAPING, 0));
            verifAndAddZaap(curMap.getId());
            SocketManager.GAME_SEND_WC_PACKET(this);
        }
    }

    public void openTrunk(int cellID) {
        Trunk.getTrunkIdByCoord(curMap.getId(), cellID).ifPresent(trunk -> {
            if (trunk.getPlayer() != null) {
                this.send("Im120");
                return;
            }
            this.setExchangeAction(new ExchangeAction<>(ExchangeAction.IN_TRUNK, trunk));
            Trunk.open(this, "-", true);
        });
    }

    public void verifAndAddZaap(int mapId) {
        if (!verifOtomaiZaap())
            return;
        if (!_zaaps.contains(mapId)) {
            _zaaps.add(mapId);
            SocketManager.GAME_SEND_Im_PACKET(this, "024");
            ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
        }
    }

    public boolean verifOtomaiZaap() {
        return Config.allZaap || !(this.getCurMap().getId() == 10643 || this.getCurMap().getId() == 11210)
                || World.world.getConditionManager().validConditions(this, "QT=231") && World.world.getConditionManager().validConditions(this, "QT=232");
    }

    public void openPrismeMenu() {
        if (this.fight == null) {
            if (getDeshonor() >= 3) {
                SocketManager.GAME_SEND_Im_PACKET(this, "183");
                return;
            }

            this.setExchangeAction(new ExchangeAction<>(ExchangeAction.IN_PRISM, 0));
            SocketManager.SEND_Wp_MENU_Prisme(this);
        }
    }

    public void useZaap(int id) {
        if (this.getExchangeAction() == null || this.getExchangeAction().getType() != ExchangeAction.IN_ZAAPING)
            return;
        if (this.fight != null || this.isInPrison()
                || !this._zaaps.contains(id) || !this._zaaps.contains(this.curMap.getId())) {
            SocketManager.GAME_SEND_WV_PACKET(this);
            return;
        }

        GameMap map = World.world.getMap(id);
        int cost = Formulas.calculZaapCost(this, this.curMap, map);

        if (this.kamas < cost) return;

        if (map == null) {
            SocketManager.GAME_SEND_WUE_PACKET(this);
            return;
        }

        GameCase cell = map.getCase(World.world.getZaapCellIdByMapId(id));
        if (cell == null || !cell.isWalkable(false)) {
            SocketManager.GAME_SEND_WUE_PACKET(this);
            return;
        }
        /*if (World.world.getMap(id).getSubArea().getArea().getSuperArea() != this.curMap.getSubArea().getArea().getSuperArea()) {
            SocketManager.GAME_SEND_WUE_PACKET(this);
            return;
        }*/
        if ((id == 4263 && this.getAlignment() == 2) || (id == 5295 && this.getAlignment() == 1))
            return;

        this.kamas -= cost;
        this.teleport(id, cell.getId());
        SocketManager.GAME_SEND_STATS_PACKET(this);//On envoie la perte de kamas
        SocketManager.GAME_SEND_WV_PACKET(this);//On ferme l'interface Zaap
        this.setExchangeAction(null);
    }

    public void usePrisme(String packet) {
        if (this.getExchangeAction() == null || this.getExchangeAction().getType() != ExchangeAction.IN_PRISM)
            return;
        int celdaID = 340;
        int MapID = 7411;
        for (Prism Prisme : World.world.AllPrisme()) {
            if (Prisme.getMap() == Short.valueOf(packet.substring(2))) {
                celdaID = Prisme.getCell();
                MapID = Prisme.getMap();
                break;
            }
        }
        int costo = Formulas.calculZaapCost(this, curMap, World.world.getMap(MapID));
        if (MapID == curMap.getId())
            costo = 0;
        if(costo < 1)
            costo = 100;
        if (kamas < costo) {
            SocketManager.GAME_SEND_MESSAGE(this, this.getLang().trans("client.player.useprisme.nokamas"));
            return;
        }
        kamas -= costo;
        SocketManager.GAME_SEND_STATS_PACKET(this);
        this.teleport(Short.valueOf(packet.substring(2)), celdaID);
        SocketManager.SEND_Ww_CLOSE_Prisme(this);
        this.setExchangeAction(null);
    }

    public String parseZaaps() {
        StringBuilder str = new StringBuilder();
        boolean first = true;

        if (_zaaps.isEmpty())
            return "";
        for (int i : _zaaps) {
            if (!first)
                str.append(",");
            first = false;
            str.append(i);
        }
        return str.toString();
    }

    public String parsePrism() {
        Prism prism = curMap.getSubArea().getPrism();
        if (prism == null) return "-3";
        else if (prism.getFight().getState() == Constant.FIGHT_STATE_PLACE)
            return "0;" + (45000 - (System.currentTimeMillis() - prism.getFight().getLaunchTime())) + ";45000;7";
        else
            return String.valueOf(prism.getFight() != null && prism.getFight().getState() == Constant.FIGHT_STATE_ACTIVE ? "-2" : "-1" ); // -1 ou 0 ?
    }

    public void stopZaaping() {
        if (this.getExchangeAction() == null || this.getExchangeAction().getType() != ExchangeAction.IN_ZAAPING)
            return;

        this.setExchangeAction(null);
        SocketManager.GAME_SEND_WV_PACKET(this);
    }

    public void Zaapi_close() {
        if (this.getExchangeAction() == null || this.getExchangeAction().getType() != ExchangeAction.IN_ZAPPI)
            return;
        this.setExchangeAction(null);
        SocketManager.GAME_SEND_CLOSE_ZAAPI_PACKET(this);
    }

    public void Prisme_close() {
        if (this.getExchangeAction() == null || this.getExchangeAction().getType() != ExchangeAction.IN_PRISM)
            return;
        this.setExchangeAction(null);
        SocketManager.SEND_Ww_CLOSE_Prisme(this);
    }

    public void Zaapi_use(String packet) {
        if (this.getExchangeAction() == null || this.getExchangeAction().getType() != ExchangeAction.IN_ZAPPI)
            return;
        GameMap map = World.world.getMap(Integer.valueOf(packet.substring(2)));

        if (map != null) {
            int cell = map.findObjectsPositionsByID(Arrays.asList(7030, 7031)).findFirst().orElse(0);
            if(cell == 0) throw new IllegalStateException(String.format("no zaapi found for map #%d", map.getId()));

            cell += 18; // Get cell below

            if (map.getSubArea() != null && (map.getSubArea().getArea().getId() == 7 || map.getSubArea().getArea().getId() == 11)) {
                int price = 20;
                if (this.getAlignment() == 1 || this.getAlignment() == 2)
                    price = 10;
                kamas -= price;
                SocketManager.GAME_SEND_STATS_PACKET(this);
                if ((map.getSubArea().getArea().getId() == 7 && this.getCurMap().getSubArea().getArea().getId() == 7)
                        || (map.getSubArea().getArea().getId() == 11 && this.getCurMap().getSubArea().getArea().getId() == 11)) {
                    this.teleport(Integer.parseInt(packet.substring(2)), cell, false);
                }
                SocketManager.GAME_SEND_CLOSE_ZAAPI_PACKET(this);
                this.setExchangeAction(null);
            }
        }
    }

    public boolean hasItemTemplate(int i, int q, boolean equipped) {
        for (GameObject obj : objects.values()) {
            if (!equipped && obj.getPosition() != Constant.ITEM_POS_NO_EQUIPED)
                continue;
            if (obj.getTemplate().getId() != i)
                continue;
            if (obj.getQuantity() >= q)
                return true;
        }
        return false;
    }

    public boolean hasItemType(int type) {
        for (GameObject obj : objects.values()) {
            if (obj.getPosition() != Constant.ITEM_POS_NO_EQUIPED)
                continue;
            if (obj.getTemplate().getType() == type)
                return true;
        }

        return false;
    }

    public GameObject getItemTemplate(int i, int q) {
        for (GameObject obj : objects.values()) {
            if (obj.getPosition() != Constant.ITEM_POS_NO_EQUIPED)
                continue;
            if (obj.getTemplate().getId() != i)
                continue;
            if (obj.getQuantity() >= q)
                return obj;
        }
        return null;
    }

    public GameObject getItemTemplate(int i) {

        for (GameObject obj : objects.values()) {
            if (obj.getTemplate().getId() != i)
                continue;
            return obj;
        }

        return null;
    }

    public int getNbItemTemplate(int i) {
        for (GameObject obj : objects.values()) {
            if (obj.getTemplate().getId() != i)
                continue;
            return obj.getQuantity();
        }
        return -1;
    }

    public boolean isDispo(Player sender) {
        return !_isAbsent && (!_isInvisible || getAccount().isFriendWith(sender.getAccount().getId()));

    }

    public boolean get_isClone() {
        return _isClone;
    }

    public void set_isClone(boolean isClone) {
        _isClone = isClone;
    }

    public byte getCurrentTitle() {
        return currentTitle;
    }

    public void setCurrentTitle(int i) {
        currentTitle = (byte) i;
    }

    //FIN CLONAGE
    public void VerifAndChangeItemPlace() {
        boolean isFirstAM = true;
        boolean isFirstAN = true;
        boolean isFirstANb = true;
        boolean isFirstAR = true;
        boolean isFirstBO = true;
        boolean isFirstBOb = true;
        boolean isFirstCA = true;
        boolean isFirstCE = true;
        boolean isFirstCO = true;
        boolean isFirstDa = true;
        boolean isFirstDb = true;
        boolean isFirstDc = true;
        boolean isFirstDd = true;
        boolean isFirstDe = true;
        boolean isFirstDf = true;
        boolean isFirstFA = true;

        for (GameObject obj : objects.values()) {
            if (obj.getPosition() == Constant.ITEM_POS_NO_EQUIPED)
                continue;
            if (obj.getPosition() == Constant.ITEM_POS_AMULETTE) {
                if (isFirstAM) {
                    isFirstAM = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_ANNEAU1) {
                if (isFirstAN) {
                    isFirstAN = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_ANNEAU2) {
                if (isFirstANb) {
                    isFirstANb = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_ARME) {
                if (isFirstAR) {
                    isFirstAR = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_BOTTES) {
                if (isFirstBO) {
                    isFirstBO = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_BOUCLIER) {
                if (isFirstBOb) {
                    isFirstBOb = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_CAPE) {
                if (isFirstCA) {
                    isFirstCA = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_CEINTURE) {
                if (isFirstCE) {
                    isFirstCE = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_COIFFE) {
                if (isFirstCO) {
                    isFirstCO = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_DOFUS1) {
                if (isFirstDa) {
                    isFirstDa = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_DOFUS2) {
                if (isFirstDb) {
                    isFirstDb = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_DOFUS3) {
                if (isFirstDc) {
                    isFirstDc = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_DOFUS4) {
                if (isFirstDd) {
                    isFirstDd = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_DOFUS5) {
                if (isFirstDe) {
                    isFirstDe = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_DOFUS6) {
                if (isFirstDf) {
                    isFirstDf = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_FAMILIER) {
                if (isFirstFA) {
                    isFirstFA = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            }
        }
    }

    //Mariage

    public Stalk getStalk() {
        return _traqued;
    }

    public void setStalk(Stalk traq) {
        _traqued = traq;
    }

    public void setWife(int id) {
        this.wife = id;
        ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
    }

    public String get_wife_friendlist() {
        Player wife = World.world.getPlayer(this.wife);
        StringBuilder str = new StringBuilder();
        if (wife != null) {
            int color1 = wife.getColor1(), color2 = wife.getColor2(), color3 = wife.getColor3();
            if (wife.getObjetByPos(Constant.ITEM_POS_MALEDICTION) != null)
                if (wife.getObjetByPos(Constant.ITEM_POS_MALEDICTION).getTemplate().getId() == 10838) {
                    color1 = 16342021;
                    color2 = 16342021;
                    color3 = 16342021;
                }
            str.append(wife.getName()).append("|").append(wife.getGfxId()).append("|").append(color1).append("|").append(color2).append("|").append(color3).append("|");
            if (!wife.isOnline()) {
                str.append("|");
            } else {
                str.append(wife.parse_towife()).append("|");
            }
        } else {
            str.append("|");
        }
        return str.toString();
    }

    public String parse_towife() {
        int f = 0;
        if (fight != null) {
            f = 1;
        }
        return curMap.getId() + "|" + this.getLevel() + "|" + f;
    }

    public void meetWife(Player p)// Se teleporter selon les sacro-saintes autorisations du mariage.
    {
        if (p == null)
            return; // Ne devrait theoriquement jamais se produire.

        if (this.getPodUsed() >= this.getMaxPod()) // Refuser la t�l�portation si on est full pods.
        {
            SocketManager.GAME_SEND_Im_PACKET(this, "170");
            return;
        }

        int dist = (curMap.getX() - p.getCurMap().getX())
                * (curMap.getX() - p.getCurMap().getX())
                + (curMap.getY() - p.getCurMap().getY())
                * (curMap.getY() - p.getCurMap().getY());
        if (dist > 100 || p.getCurMap().getId() == this.getCurMap().getId())// La distance est trop grande...
        {
            if (p.getSexe() == 0)
                SocketManager.GAME_SEND_Im_PACKET(this, "178");
            else
                SocketManager.GAME_SEND_Im_PACKET(this, "179");
            return;
        }

        int cellPositiontoadd = Constant.getNearestCellIdUnused(p);
        if (cellPositiontoadd == -1) {
            if (p.getSexe() == 0)
                SocketManager.GAME_SEND_Im_PACKET(this, "141");
            else
                SocketManager.GAME_SEND_Im_PACKET(this, "142");
            return;
        }

        teleport(p.getCurMap().getId(), cellPositiontoadd);
    }

    public void Divorce() {
        if (isOnline())
            SocketManager.GAME_SEND_Im_PACKET(this, "047;"
                    + World.world.getPlayer(wife).getName());

        wife = 0;
        ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
    }

    public int getWife() {
        return wife;
    }

    public int setisOK(int ok) {
        return _isOK = ok;
    }

    public int getisOK() {
        return _isOK;
    }

    public List<GameObject> getEquippedObjects() {
        List<GameObject> objects = new ArrayList<>();
        synchronized(objects) {
            this.objects.values().stream().filter(object -> object.getPosition() != -1 && object.getPosition() < 34).forEach(objects::add);
        }
        return objects;
    }

    public void changeOrientation(int toOrientation) {
        if (this.get_orientation() == 0 || this.get_orientation() == 2
                || this.get_orientation() == 4 || this.get_orientation() == 6) {
            this.set_orientation(toOrientation);
            SocketManager.GAME_SEND_eD_PACKET_TO_MAP(getCurMap(), this.getId(), toOrientation);
        }
    }

    /** Heroic **/
    private byte dead = 0, deathCount = 0, deadType = 0;
    private short deadLevel = 0;
    private long deadTime = 0, killByTypeId = 0, totalKills = 0;

    public byte isDead() {
        return dead;
    }

    public void setDead(byte dead) {
        this.dead = dead;
    }

    public short getDeadLevel() {
        return deadLevel;
    }

    public byte getDeathCount() {
        return deathCount;
    }

    public void increaseTotalKills() {
        this.totalKills++;
    }

    public long getTotalKills() {
        return totalKills;
    }

    public String getDeathInformation() {
        return dead + "," + deadTime + "," + deadType + "," + killByTypeId + "," + deadLevel;
    }

    public void die(byte type, long id) {
        new ArrayList<>(this.getItems().values()).stream().filter(Objects::nonNull).forEach(object -> this.removeItem(object.getGuid(), object.getQuantity(), true, false));
        this.setFuneral();
        this.deathCount++;
        this.deadLevel = (short) this.getLevel();
        this.deadType = type;
        this.killByTypeId = id;
    }

    public void revive() {
        int revive = ((PlayerData) DatabaseManager.get(PlayerData.class)).canRevive(this);

        if(revive == 1) {
            this.curMap = World.world.getMap( 7411);
            this.curCell = World.world.getMap( 7411).getCase(311);
        } else {
            if(this._morphMode) this.unsetFullMorph();
            if (this.getParty() != null) this.getParty().leave(this);
            this.resetVars();
            this.getStats().addOneStat(125, -this.getStats().getEffect(125));
            this.getStats().addOneStat(124, -this.getStats().getEffect(124));
            this.getStats().addOneStat(118, -this.getStats().getEffect(118));
            this.getStats().addOneStat(123, -this.getStats().getEffect(123));
            this.getStats().addOneStat(119, -this.getStats().getEffect(119));
            this.getStats().addOneStat(126, -this.getStats().getEffect(126));
            this.addCapital(-this.getCapital());
            this.setSpellPoints(0);
            this.getStatsParcho().getEffects().clear();
            this._sorts.clear();
            this._sorts.putAll(Constant.getStartSorts(classe));
            this._sortsPlaces.clear();
            this._sortsPlaces.putAll(Constant.getStartSortsPlaces(classe));
            if(this.level >= 100)
                this.stats.addOneStat(Constant.STATS_ADD_PA, -1);
            this.level = 1;
            this.exp = 0;
            this.curMap = World.world.getMap(Constant.getStartMap(this.classe));
            this.curCell = this.curMap.getCase(Constant.getStartCell(this.classe));
            this._honor = 0;
            this._deshonor = 0;
            this.alignment = 0;
            this.kamas = 0;
            //this._metiers.clear();
            if(this._mount != null) {
                for(GameObject gameObject : this._mount.getObjects().values())
                    World.world.removeGameObject(gameObject.getGuid());
                this._mount.getObjects().clear();

                this.setMount(null);
                this.setMountGiveXp(0);
            }
        }

        this.isGhost = false;
        this.dead = 0;
        this.setEnergy(10000);
        this.setGfxId(Integer.parseInt(this.getClasse() + "" + this.getSexe()));
        this.setCanAggro(true);
        this.setAway(false);
        this.setSpeed(0);

        ((PlayerData) DatabaseManager.get(PlayerData.class)).setRevive(this);
    }
    /** End heroic **/

    public boolean isGhost() {
        return this.isGhost;
    }

    public void setGhost(boolean ghost) {
        this.isGhost = ghost;
    }

    public void setFuneral() {
        this.dead = 1;
        this.deadTime = System.currentTimeMillis();
        this.setEnergy(-1);
        if (this.isOnMount())
            this.toogleOnMount();
        if (this.get_orientation() == 2) {
            this.set_orientation(1);
            SocketManager.GAME_SEND_eD_PACKET_TO_MAP(this.getCurMap(), this.getId(), 1);
        }
        this.setGfxId(Integer.parseInt(this.getClasse() + "3"));
        SocketManager.send(this, "AR3K");//Block l'orientation
        SocketManager.send(this, "M112");//T'es mort!!! t'es mort!!! Mouhhahahahahaaaarg
        SocketManager.GAME_SEND_ALTER_GM_PACKET(getCurMap(), this);
    }

    public void setGhost() {
        if (isOnMount())
            toogleOnMount();
        if (Config.modeHeroic) {
            this.setGfxId(Integer.parseInt(this.getClasse() + "" + this.getSexe()));
            this.send("GO");
            return;
        }

        this.dead = 0;
        this.isGhost = true;
        this.setEnergy(0);
        setGfxId(8004);
        setCanAggro(false);
        setAway(true);
        setSpeed(-40);
        this.regenRate = 0;
        SocketManager.send(this, "AR6bk");

        Optional<SubArea> subArea = Optional.ofNullable(this.getCurMap()).map(GameMap::getSubArea);
        if (!subArea.isPresent()) {
            return;
        }

        // TODO: Refactor that mess
        String phoenixList = subArea
            .map(SubArea::getArea)
            .map(Area::getSuperArea)
            .map(superArea -> {
                if(superArea == INCARNAM_SUPERAREA) {
                    return "1;5";
                }
                return null;
            }).orElse(Constant.ALL_PHOENIX);
        SocketManager.send(this, "IH" + phoenixList);

        Constant.tpCim(this);
    }

    public void setAlive() {
        if (!this.isGhost)
            return;
        this.isGhost = false;
        this.dead = 0;
        this.setEnergy(1000);
        this.setPdv(1);
        this.setGfxId(Integer.parseInt(this.getClasse() + "" + this.getSexe()));
        this.setCanAggro(true);
        this.setAway(false);
        this.setSpeed(0);
        SocketManager.GAME_SEND_MESSAGE(this, "Tu as gagné <b>1000</b> points d'énergie.", "009900");
        SocketManager.GAME_SEND_STATS_PACKET(this);
        SocketManager.GAME_SEND_ALTER_GM_PACKET(this.curMap, this);
        SocketManager.send(this, "IH");
        SocketManager.send(this, "AR6bk");//Block l'orientation
    }

    public Map<Integer, Integer> getStoreItems() {
        return _storeItems;
    }

    public Fight getLastFight() {
        return lastFight;
    }

    public void setLastFightForEndFightAction(Fight fight) {
        this.endFightAction = null;
        this.lastFight = fight;
    }

    public void setNeededEndFightAction(Fight f, Action endFightAction) {
        this.lastFight = f;
        this.endFightAction = endFightAction;
    }

    public boolean applyEndFightAction() {
        if(this.endFightAction == null) {
            return false;
        }
        this.endFightAction.apply(this, null, -1, -1, getCurMap());
        this.endFightAction = null;
        return true;
    }

    public String parseStoreItemsList() {
        StringBuilder list = new StringBuilder();
        if (_storeItems.isEmpty())
            return "";
        for (Entry<Integer, Integer> obj : _storeItems.entrySet()) {
            GameObject O = World.world.getGameObject(obj.getKey());
            if (O == null)
                continue;
            //O.getPoidOfBaseItem(O.getPlayerId());
            list.append(O.getGuid()).append(";").append(O.getQuantity()).append(";").append(O.getTemplate().getId()).append(";").append(O.encodeStats()).append(";").append(obj.getValue()).append("|");
        }

        return (list.length() > 0 ? list.toString().substring(0, list.length() - 1) : list.toString());
    }

    public int parseStoreItemsListPods() {
        if (_storeItems.isEmpty())
            return 0;
        int total = 0;
        for (Entry<Integer, Integer> obj : _storeItems.entrySet()) {
            GameObject O = World.world.getGameObject(obj.getKey());
            if (O != null) {
                int qua = O.getQuantity();
                int poidBase1 = O.getTemplate().getPod() * qua;
                total += poidBase1;
            }
        }
        return total;
    }

    public String parseStoreItemstoBD() {
        StringBuilder str = new StringBuilder();
        for (Entry<Integer, Integer> _storeObjets : _storeItems.entrySet()) {
            str.append(_storeObjets.getKey()).append(",").append(_storeObjets.getValue()).append("|");
        }

        return str.toString();
    }

    public void addInStore(int ObjID, int price, int qua) {
        GameObject PersoObj = World.world.getGameObject(ObjID);
        //Si le joueur n'a pas l'item dans son sac ...
        if (_storeItems.get(ObjID) != null) {
                _storeItems.remove(ObjID);
                _storeItems.put(ObjID, price);
                SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
                return;
        }

        if (objects.get(ObjID) == null) {
            GameServer.a();
            return;
        }

        if(PersoObj == null || PersoObj.isAttach()) return;

        //Si c'est un item �quip� ...
        if (PersoObj.getPosition() != Constant.ITEM_POS_NO_EQUIPED)
            return;

        GameObject SimilarObj = getSimilarStoreItem(PersoObj);
        int newQua = PersoObj.getQuantity() - qua;
        if (SimilarObj == null)//S'il n'y pas d'item du meme Template
        {
            //S'il ne reste pas d'item dans le sac
            if (newQua <= 0) {
                //On enleve l'objet du sac du joueur
                removeItem(PersoObj.getGuid());
                //On met l'objet du sac dans le store, avec la meme quantit�
                _storeItems.put(PersoObj.getGuid(), price);
                SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, PersoObj.getGuid());
                SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
            } else
            //S'il reste des objets au joueur
            {
                //on modifie la quantit� d'item du sac
                PersoObj.setQuantity(newQua);
                //On ajoute l'objet a la banque et au monde
                SimilarObj = PersoObj.getClone(qua, true);
                World.world.addGameObject(SimilarObj);
                _storeItems.put(SimilarObj.getGuid(), price);

                //Envoie des packets
                SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
                SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);

            }
        } else
        // S'il y avait un item du meme template
        {
            //S'il ne reste pas d'item dans le sac
            if (newQua <= 0) {
                //On enleve l'objet du sac du joueur
                removeItem(PersoObj.getGuid());
                //On enleve l'objet du monde
                World.world.removeGameObject(PersoObj.getGuid());
                //On ajoute la quantit� a l'objet en banque
                SimilarObj.setQuantity(SimilarObj.getQuantity() + PersoObj.getQuantity());

                _storeItems.remove(SimilarObj.getGuid());
                _storeItems.put(SimilarObj.getGuid(), price);

                //on envoie l'ajout a la banque de l'objet
                SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
                //on envoie la supression de l'objet du sac au joueur
                SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, PersoObj.getGuid());
            } else
            //S'il restait des objets
            {
                //on modifie la quantit� d'item du sac
                PersoObj.setQuantity(newQua);
                SimilarObj.setQuantity(SimilarObj.getQuantity() + qua);

                _storeItems.remove(SimilarObj.getGuid());
                _storeItems.put(SimilarObj.getGuid(), price);

                SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
                SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);

            }
        }
        SocketManager.GAME_SEND_Ow_PACKET(this);
        ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
    }

    private GameObject getSimilarStoreItem(GameObject exGameObject) {
        for (Integer id : _storeItems.keySet()) {
            GameObject gameObject = World.world.getGameObject(id);
            if (World.world.getConditionManager().stackIfSimilar(gameObject, exGameObject, true))
                return gameObject;
        }

        return null;
    }

    public void removeFromStore(int guid, int qua) {
        GameObject SimilarObj = World.world.getGameObject(guid);
        //Si le joueur n'a pas l'item dans son store ...
        if (_storeItems.get(guid) == null) {
            GameServer.a();
            return;
        }

        GameObject PersoObj = getSimilarItem(SimilarObj);
        int newQua = SimilarObj.getQuantity() - qua;
        if (PersoObj == null)//Si le joueur n'avait aucun item similaire
        {
            //S'il ne reste rien en store
            if (newQua <= 0) {
                //On retire l'item du store
                _storeItems.remove(guid);
                //On l'ajoute au joueur
                objects.put(guid, SimilarObj);

                //On envoie les packets
                SocketManager.GAME_SEND_OAKO_PACKET(this, SimilarObj);
                SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
            }
        } else {
            //S'il ne reste rien en store
            if (newQua <= 0) {
                //On retire l'item de la banque
                _storeItems.remove(SimilarObj.getGuid());
                World.world.removeGameObject(SimilarObj.getGuid());
                //On Modifie la quantit� de l'item du sac du joueur
                PersoObj.setQuantity(PersoObj.getQuantity()
                        + SimilarObj.getQuantity());
                //On envoie les packets
                SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
                SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
            }
        }
        SocketManager.GAME_SEND_Ow_PACKET(this);
        ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
    }

    public void removeStoreItem(int guid) {
        _storeItems.remove(guid);
    }

    public void addStoreItem(int guid, int price) {
        _storeItems.put(guid, price);
    }

    public int getSpeed() {
        return _Speed;
    }

    public void setSpeed(int _Speed) {
        this._Speed = _Speed;
    }

    public int get_savestat() {
        return this.savestat;
    }

    public void set_savestat(int stat) {
        this.savestat = stat;
    }

    public boolean getMetierPublic() {
        return _metierPublic;
    }

    public void setMetierPublic(boolean b) {
        _metierPublic = b;
    }

    public boolean getLivreArtisant() {
        return _livreArti;
    }

    public void setLivreArtisant(boolean b) {
        _livreArti = b;
    }

    public boolean hasSpell(int spellID) {
        return (getSortStatBySortIfHas(spellID) != null);
    }

    public void leaveEnnemyFaction() {
        if (!isInEnnemyFaction)
            return;//pas en prison on fait pas la commande
        int pGrade = this.getGrade();
        long compar = System.currentTimeMillis()
                - (enteredOnEnnemyFaction + 60000 * pGrade);

        switch (pGrade) {
            case 1:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage(this.getLang().trans("client.player.jail.free.oneminute", "1"));
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage(this.getLang().trans("client.player.jail.free.wait", restant / 1000));
                }
                break;
            case 2:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage(this.getLang().trans("client.player.jail.free.after", "2"));
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage(this.getLang().trans("client.player.jail.free.wait", restant / 1000));
                }
                break;
            case 3:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage(this.getLang().trans("client.player.jail.free.after", "3"));
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage(this.getLang().trans("client.player.jail.free.wait", restant / 1000));
                }
                break;
            case 4:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage(this.getLang().trans("client.player.jail.free.after", "4"));
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage(this.getLang().trans("client.player.jail.free.wait", restant / 1000));
                }
                break;
            case 5:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage(this.getLang().trans("client.player.jail.free.after", "5"));
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage(this.getLang().trans("client.player.jail.free.wait", restant / 1000));
                }
                break;
            case 6:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage(this.getLang().trans("client.player.jail.free.after", "6"));
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage(this.getLang().trans("client.player.jail.free.wait", restant / 1000));
                }
                break;
            case 7:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage(this.getLang().trans("client.player.jail.free.after", "7"));
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage(this.getLang().trans("client.player.jail.free.wait", restant / 1000));
                }
                break;
            case 8:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage(this.getLang().trans("client.player.jail.free.after", "8"));
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage(this.getLang().trans("client.player.jail.free.wait", restant / 1000));
                }
                break;
            case 9:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage(this.getLang().trans("client.player.jail.free.after", "9"));
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage(this.getLang().trans("client.player.jail.free.wait", restant / 1000));
                }
                break;
            case 10:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage(this.getLang().trans("client.player.jail.free.after", "10"));
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage(this.getLang().trans("client.player.jail.free.wait", restant / 1000));
                }
                break;
        }
        ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
    }

    public void leaveEnnemyFactionAndPay(Player perso) {
        if (!isInEnnemyFaction)
            return;//pas en prison on fait pas la commande
        int pGrade = perso.getGrade();
        long curKamas = perso.getKamas();
        switch (pGrade) {
            case 1:
                if (curKamas < 1000) {
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 1000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 2:
                if (curKamas < 2000) {
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 2000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 3:
                if (curKamas < 3000) {
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 3000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 4:
                if (curKamas < 4000) {
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 4000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 5:
                if (curKamas < 5000) {
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 5000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 6:
                if (curKamas < 7000) {
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 7000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 7:
                if (curKamas < 9000) {
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 9000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 8:
                if (curKamas < 12000) {
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 12000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 9:
                if (curKamas < 16000) {
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 16000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 10:
                if (curKamas < 25000) {
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 25000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
        }
        ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
        SocketManager.GAME_SEND_STATS_PACKET(perso);
    }

    public void leaveFaction() {
        try {
            isInEnnemyFaction = false;
            enteredOnEnnemyFaction = 0;
            warpToSavePos();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void teleportWithoutBlocked(int newMapID, int newCellID)//Aucune condition genre <<en_prison>> etc
    {
        GameClient PW = null;
        if (getAccount().getGameClient() != null) {
            PW = getAccount().getGameClient();
        }
        if (World.world.getMap(newMapID) == null) {
            GameServer.a();
            return;
        }
        if (World.world.getMap(newMapID).getCase(newCellID) == null) {
            GameServer.a();
            return;
        }
        if (PW != null) {
            SocketManager.GAME_SEND_GA2_PACKET(PW, this.getId());
            SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(curMap, this.getId());
        }
        curCell.removePlayer(this);
        curMap = World.world.getMap(newMapID);
        curCell = curMap.getCase(newCellID);

        //Verification de la Map
        //Verifier la validit� du mountpark
        if (curMap.getMountPark() != null
                && curMap.getMountPark().getOwner() > 0
                && curMap.getMountPark().getGuild().getId() != -1) {
            if (World.world.getGuild(curMap.getMountPark().getGuild().getId()) == null)//Ne devrait pas arriver
            {
                GameServer.a();
                GameMap.removeMountPark(curMap.getMountPark().getGuild().getId());
            }
        }
        //Verifier la validit� du Collector
        if (Collector.getCollectorByMapId(curMap.getId()) != null) {
            if (World.world.getGuild(Collector.getCollectorByMapId(curMap.getId()).getGuildId()) == null)//Ne devrait pas arriver
            {
                GameServer.a();
                Collector.removeCollector(Collector.getCollectorByMapId(curMap.getId()).getGuildId());
            }
        }

        if (PW != null) {
            SocketManager.GAME_SEND_MAPDATA(PW, newMapID, curMap.getDate(), curMap.getKey());
            curMap.addPlayer(this);
        }

        if (!follower.isEmpty())//On met a jour la Map des personnages qui nous suivent
        {
            for (Player t : follower.values()) {
                if (t.isOnline())
                    SocketManager.GAME_SEND_FLAG_PACKET(t, this);
                else
                    follower.remove(t.getId());
            }
        }
    }

    public void teleportFaction(int factionEnnemy) {
        int mapID = 0;
        int cellID = 0;
        enteredOnEnnemyFaction = System.currentTimeMillis();
        isInEnnemyFaction = true;

        switch (factionEnnemy) {
            case 1://bonta
                mapID = (short) 6164;
                cellID = 236;
                break;

            case 2://brakmar
                mapID = (short) 6171;
                cellID = 397;
                break;

            case 3://Seriane
                mapID = (short) 1002;
                cellID = 326;
                break;

            default://neutre(WTF? XD)
                mapID = (short) 8534;
                cellID = 297;
                break;
        }
        this.sendMessage(this.getLang().trans("client.player.jail.ask.waiting"));
        if (this.getEnergy() <= 0) {
            if (isOnMount())
                toogleOnMount();
            this.isGhost = true;
            setGfxId(8004);
            setCanAggro(false);
            setAway(true);
            setSpeed(-40);
        }
        teleportWithoutBlocked(mapID, cellID);
        ((PlayerData) DatabaseManager.get(PlayerData.class)).update(this);
    }

    public String parsecolortomount() {
        int color1 = this.getColor1(), color2 = this.getColor2(), color3 = this.getColor3();
        if (this.getObjetByPos(Constant.ITEM_POS_MALEDICTION) != null)
            if (this.getObjetByPos(Constant.ITEM_POS_MALEDICTION).getTemplate().getId() == 10838) {
                color1 = 16342021;
                color2 = 16342021;
                color3 = 16342021;
            }
        return (color1 == -1 ? "" : Integer.toHexString(color1)) + ","
                + (color2 == -1 ? "" : Integer.toHexString(color2)) + ","
                + (color3 == -1 ? "" : Integer.toHexString(color3));
    }

    //region Objects class
    public Map<Integer, World.Couple<Integer, Integer>> getObjectsClassSpell() {
        return objectsClassSpell;
    }

    public void addObjectClassSpell(int spell, int effect, int value) {
        if (!objectsClassSpell.containsKey(spell)) {
            objectsClassSpell.put(spell, new World.Couple<>(effect, value));
        }
    }

    public void removeObjectClassSpell(int spell) {
        if (objectsClassSpell.containsKey(spell)) {
            objectsClassSpell.remove(spell);
        }
    }

    public void addObjectClass(int item) {
        if (!objectsClass.contains(item))
            objectsClass.add(item);
    }

    public void removeObjectClass(int item) {
        if (objectsClass.contains(item)) {
            int index = objectsClass.indexOf(item);
            objectsClass.remove(index);
        }
    }

    public void refreshObjectsClass() {
        for (int position = 2; position < 8; position++) {
            GameObject object = getObjetByPos(position);

            if(object != null) {
                ObjectTemplate template = object.getTemplate();
                int set = object.getTemplate().getPanoId();

                if (template != null && set >= 81 && set <= 92) {
                    String[] stats = object.getTemplate().getStrTemplate().split(",");
                    for (String stat : stats) {
                        String[] split = stat.split("#");
                        int effect = Integer.parseInt(split[0], 16), spell = Integer.parseInt(split[1], 16);
                        int value = Integer.parseInt(split[3], 16);
                        if(effect == 289)
                            value = 1;
                        SocketManager.SEND_SB_SPELL_BOOST(this, effect + ";" + spell + ";" + value);
                        addObjectClassSpell(spell, effect, value);
                    }

                    if (!this.objectsClass.contains(template.getId()))
                        this.objectsClass.add(template.getId());
                }
            }
        }
    }

    public int getValueOfClassObject(int spell, int effect) {
        if (this.objectsClassSpell.containsKey(spell)) {
            if (this.objectsClassSpell.get(spell).first == effect) {
               return this.objectsClassSpell.get(spell).second;
            }
        }
        return 0;
    }
    //endregion

    public int storeAllBuy() {
        int total = 0;
        for (Entry<Integer, Integer> value : _storeItems.entrySet()) {
            GameObject O = World.world.getGameObject(value.getKey());
            int multiple = O.getQuantity();
            int add = value.getValue() * multiple;
            total += add;
        }

        return total;
    }

    public void DialogTimer() {
        TimerWaiter.addNext(() -> {
            if (this.getExchangeAction() == null || this.getExchangeAction().getType() != ExchangeAction.TRADING_WITH_COLLECTOR)
                return;
            if ((Integer) this.getExchangeAction().getValue() != 0) {
                Collector collector = World.world.getCollector((Integer) this.getExchangeAction().getValue());
                if (collector == null)
                    return;
                collector.reloadTimer();
                for (Player z : World.world.getGuild(collector.getGuildId()).getPlayers()) {
                    if (z == null)
                        continue;
                    if (z.isOnline()) {
                        SocketManager.GAME_SEND_gITM_PACKET(z, Collector.parseToGuild(z.getGuild().getId()));
                        String str = "G" + collector.getFullName() + "|.|" + World.world.getMap(collector.getMap()).getX() + "|" + World.world.getMap(collector.getMap()).getY() + "|" + getName() + "|" + collector.getXp() + ";";

                        if (!collector.getLogObjects().equals(""))
                            str += collector.getLogObjects();

                        Player.this.getGuildMember().giveXpToGuild(collector.getXp());
                        SocketManager.GAME_SEND_gT_PACKET(z, str);
                    }
                }
                getCurMap().RemoveNpc(collector.getId());
                SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(getCurMap(), collector.getId());
                collector.delCollector(collector.getId());
                ((CollectorData) DatabaseManager.get(CollectorData.class)).delete(collector);
            }
            ((PlayerData) DatabaseManager.get(PlayerData.class)).update(getAccount().getCurrentPlayer());
            SocketManager.GAME_SEND_EV_PACKET(getGameClient());
            setAway(false);
        }, 5, TimeUnit.MINUTES);
    }

    public long getTimeTaverne() {
        return timeTaverne;
    }

    public void setTimeTaverne(long timeTaverne) {
        this.timeTaverne = timeTaverne;
        ((PlayerData) DatabaseManager.get(PlayerData.class)).updateTimeTaverne(this);
    }

    public GameAction getGameAction() {
        return _gameAction;
    }

    public void setGameAction(GameAction Action) {
        _gameAction = Action;
    }

    public int getAlignMap() {
        if (this.getCurMap().getSubArea() == null)
            return -1;
        if (this.getCurMap().getSubArea().getAlignment() == 0)
            return 1;
        if (this.getCurMap().getSubArea().getAlignment() == this.getAlignment())
            return 1;
        return -1;
    }

    public List<Integer> getEmotes() {
        return emotes;
    }

    public boolean addStaticEmote(int emote) {
        if (this.emotes.contains(emote))
            return false;
        this.emotes.add(emote);
        if (!isOnline())
            return true;
        SocketManager.GAME_SEND_EMOTE_LIST(this, getCompiledEmote(getEmotes()));
        SocketManager.GAME_SEND_STATS_PACKET(this);
        SocketManager.send(this, "eA" + emote);
        return true;
    }

    public String parseEmoteToDB() {
        StringBuilder str = new StringBuilder();
        boolean isFirst = true;
        for (int i : emotes) {
            if (isFirst)
                str.append(i).append("");
            else
                str.append(";").append(i);
            isFirst = false;
        }
        return str.toString();
    }

    public boolean getBlockMovement() {
        return this.isBlocked;
    }

    public void setBlockMovement(boolean b) {
        this.isBlocked = b;
    }

    public GameClient getGameClient() {
        return this.getAccount() != null ? this.getAccount().getGameClient() : null;
    }

    public void send(String packet) {
        SocketManager.send(this, packet);
    }

    public void sendMessage(String msg) {
        SocketManager.GAME_SEND_MESSAGE(this, msg);
    }

    public void sendTypeMessage(String name, String msg) {
        this.send("Im116;<b>" + name + "</b>~" + msg);
    }

    public void sendServerMessage(String msg) {
        this.sendTypeMessage("Server", msg);
    }

    public boolean isSubscribe() {
        return !Config.subscription || this.getAccount().isSubscribe();
    }

    public boolean isMissingSubscription() {
        boolean ok = Config.subscription;

        if (this.curMap == null)
            return false;
        switch (this.curMap.getId()) {
            case 6824:
            case 6825:
            case 6826:
                return false;
        }
        if (this.curMap.getSubArea() == null)
            return false;
        if (this.curMap.getSubArea().getArea() == null)
            return false;
        if (this.curMap.getSubArea().getArea().getSuperArea() == 3
                || this.curMap.getSubArea().getArea().getSuperArea() == 4
                || this.curMap.getSubArea().getArea().getId() == 18)
            ok = false;

        return ok;
    }

    public boolean cantDefie() {
        return curMap.data.noDefy;
    }

    public boolean cantAgro() {
        return curMap.data.noAgro;
    }

    public boolean cantCanal() {
        return curMap.data.noCanal;
    }

    public boolean cantTP() {
        return this.isInPrison() || curMap.data.noTp || EventManager.isInEvent(this);
    }

    public boolean isInPrison() {
        if (this.curMap == null)
            return false;

        switch (this.curMap.getId()) {
            case 666:
            case 8726:
                return true;
        }
        return false;
    }

    public void addQuestProgression(QuestProgress qProgress) {
        getAccount().addQuestProgression(qProgress);
    }

    public void delQuestProgress(QuestProgress qProgress) {
        getAccount().delQuestProgress(qProgress);
    }

    public QuestProgress getQuestProgress(int questId) {
        return getAccount().getQuestProgress(this.id, questId);
    }

    public Optional<QuestProgress> getQuestProgressForCurrentStep(int stepId) {
        return getAccount().getQuestProgressions(this.id).filter(qp -> qp.getCurrentStep() == stepId).findFirst();
    }


    public Stream<QuestProgress> getQuestProgressions() {
        return getAccount().getQuestProgressions(this.id);
    }

    public void sendQuestStatus(int questId) {
        QuestProgress qp = getAccount().getQuestProgress(this.id, questId);

        if(qp == null) {
            throw new NullPointerException("sendQuestStatus called for non current quest");
        }

        // Call lua to get quest info
        QuestInfo qi = DataScriptVM.getInstance().handlers.questInfo(this, questId, qp.getCurrentStep());
        if(qi == null) {
            throw new NullPointerException("sendQuestStatus called for unknown quest");
        }

        StringJoiner sj = new StringJoiner("|");
        sj.add("QS"+String.join(";",
            Objects.toString(questId),
            qi.isAccountBound?"1":"0",
            qi.isRepeatable?"1":"0"
        ));


        sj.add(String.valueOf(qp.getCurrentStep()));
        sj.add(qi.objectives.stream().map(oId -> {
            String completedStr = qp.hasCompletedObjective(oId)?"1":"0";
            return oId+","+completedStr;
        }).collect(Collectors.joining(";")));
        sj.add(Objects.toString(qi.previous, ""));
        sj.add(Objects.toString(qi.next, ""));
        if (qi.question != null) {
            sj.add(Objects.toString(qi.question));
        }

        send(sj.toString());
    }

    public String encodeQuestList() {
        return "QL+" + getAccount().getQuestProgressions(this.id).
            map(qp -> {
                QuestInfo qi = DataScriptVM.getInstance().handlers.questInfo(this, qp.questId, qp.getCurrentStep());

                return String.join(";",
                    String.valueOf(qp.questId),
                    qp.isFinished()?"1":"0",
                    "", // List sort order. AccountBound/Repeatable quests tend to appear last (higher weight)
                    qi.isAccountBound?"1":"0",
                    qi.isRepeatable?"1":"0"
                );
            }).collect(Collectors.joining("|"));
    }

    public void saveQuestProgress() {
        getAccount().saveQuestProgress();
    }

    public House getInHouse() {
        return _curHouse;
    }

    public void setInHouse(House h) {
        _curHouse = h;
    }

    private ExchangeAction<?> exchangeAction;

    public ExchangeAction<?> getExchangeAction() {
        return exchangeAction;
    }

    public synchronized void setExchangeAction(ExchangeAction<?> exchangeAction) {
        if(exchangeAction == null) this.setAway(false);
        this.exchangeAction = exchangeAction;
    }

    public void refreshCraftSecure(boolean unequip) {
        // Optimize by directly checking the job for the equipped tool
        for (Player player : this.getCurMap().getPlayers()) {
            if(player == null) continue;

            GameObject object = player.getObjetByPos(Constant.ITEM_POS_ARME);
            if (object == null) {
                if (unequip) {
                    for(Player target : this.getCurMap().getPlayers())
                        target.send("EW+" + player.getId() + "|");
                }
                continue;
            }
            int toolID = object.getTemplate().getId();

            List<Integer> availableSkills = new ArrayList<>();
            for (Job job : player.getJobs()) {
                if (job.getSkills().isEmpty())
                    continue;
                if (!job.isValidTool(toolID))
                    continue;

                // Compute list of skills this player can use on this map
                this.getCurMap().data.interactiveObjects().values().stream()
                    .map(job.getSkills()::get) // Get possibles skills on this object
                    .filter(Objects::nonNull)   // Make sure we have one
                    .flatMap(List::stream)  // Group all possible skills from all objects in one list
                    .forEach(availableSkills::add);
            }

            if(availableSkills.isEmpty()) continue;

            String packet = "EW+" + player.getId() + "|" + availableSkills.stream().distinct().map(String::valueOf).collect(Collectors.joining(";"));
            for(Player target : this.getCurMap().getPlayers()) {
                if (target == null) continue;

                target.send(packet);
            }
        }
    }

    public void setFullMorphbouf(int team) {

        if (this.isOnMount()) this.toogleOnMount();
        if (_morphMode)
            unsetFullMorph();
        if (this.isGhost) {
            SocketManager.send(this, "Im1185");
            return;
        }

        _saveSpellPts = _spellPts;
        _saveSorts.putAll(_sorts);
        _saveSortsPlaces.putAll(_sortsPlaces);


        _morphMode = true;
        _sorts.clear();
        _sortsPlaces.clear();
        _spellPts = 0;
        this.Savecolors = this.color1 + "," + this.color2 + "," + this.color3;


        if(team == 0)//rouge
        {
            this.color1 = 16713479;
            this.color2 = 16777215;
            this.color3 = 16718620;
        }else//bleu
        {
            this.color1 = 360441;
            this.color2 = 94461;
            this.color3 = 486135;
        }
        if (this.fight == null)
            SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
        parseSpellsFullMorph("143;5;b,689;5;c,151;5;d,50;5;e,449;1;f");
        if (this.fight == null) {
            //SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
            //SocketManager.GAME_SEND_ASK(this.getGameClient(), this);
            SocketManager.GAME_SEND_SPELL_LIST(this);
        }

        this.Savestats = this.maxPdv + "," + this.pa + ","
                + this.pm + ","  + this.vitalite + "," + this.sagesse + ","
                + this.terre + "," + this.feu + "," + this.eau + "," + this.air
                + "," + this.initiative;
        this.maxPdv = 1000;
        this.setPdv(this.getMaxPdv());
        this.pa = 6;
        this.pm = 4;
        this.vitalite = 1000;
        this.sagesse = 100;
        this.terre = 0;
        this.feu = 0;
        this.eau = 0;
        this.air = 0;
        this.initiative = Formulas.getRandomValue(1, 100);
        this.useStats = true;
        this.donjon = false;
        this.useCac = false;
        if (this.fight == null)
            SocketManager.GAME_SEND_STATS_PACKET(this);
    }

    public void unsetFullMorphbouf() {
        if (!_morphMode)
            return;

        int morphID = this.getClasse() * 10 + this.getSexe();
        setGfxId(morphID);

        useStats = false;
        donjon = false;
        _morphMode = false;
        this.useCac = true;
        _sorts.clear();
        _sortsPlaces.clear();
        _spellPts = _saveSpellPts;
        _sorts.putAll(_saveSorts);
        _sortsPlaces.putAll(_saveSortsPlaces);
        String[] stats = this.Savestats.split(",");

        this.maxPdv = Integer.parseInt(stats[0]);
        this.pa = Integer.parseInt(stats[1]);
        this.pm = Integer.parseInt(stats[2]);
        this.vitalite = Integer.parseInt(stats[3]);
        this.sagesse = Integer.parseInt(stats[4]);
        this.terre = Integer.parseInt(stats[5]);
        this.feu = Integer.parseInt(stats[6]);
        this.eau = Integer.parseInt(stats[7]);
        this.air = Integer.parseInt(stats[8]);
        this.initiative = Integer.parseInt(stats[9]);

        String[] color = this.Savecolors.split(",");

        this.color1 = Integer.parseInt(color[0]);
        this.color2 = Integer.parseInt(color[1]);
        this.color3 = Integer.parseInt(color[2]);

        parseSpells(encodeSpellsToDB(), true);
        SocketManager.GAME_SEND_SPELL_LIST(this);
        SocketManager.GAME_SEND_STATS_PACKET(this);
        SocketManager.GAME_SEND_ALTER_GM_PACKET(this.curMap, this);
    }


    public LangEnum getLang() {
        if(this.getGameClient() != null)
            return this.getGameClient().getLanguage();
        return LangEnum.ENGLISH;
    }

    @Override
    public SPlayer scripted() {
        return this.scriptVal;
    }

    public boolean consumeCurrency(Currency cur, long qua) {
        if(cur == Currency.KAMAS) return modKamasDisplay(-qua);
        if(cur == Currency.POINTS) return getAccount().modPoints(-qua);
        if(cur.isItem()) return removeItemByTemplateId(cur.item().getId(), (int)qua, false);
        throw new RuntimeException("unknown currency type");
    }

    public boolean moveItemShortcutSend(int oldPos, int newPos) {
        ItemHash hash = _itemShortcuts.getOrDefault(oldPos, null);
        if(hash == null) return false;

        return addItemHashShortcutSend(newPos, hash);
    }

    private boolean addItemHashShortcutSend(int position, ItemHash hash) {
        // Free up previously used slot if needed
        removeItemShortcutByHash(hash).ifPresent(p -> {
            send("OrR"+p);
        });

        _itemShortcuts.put(position, hash);
        send("OrA"+ String.join(";",
                String.valueOf(position),
                String.valueOf(hash.templateId),
                hash.strStats
        ));
        return true;
    }

    public boolean addItemShortcutSend(int position, int itemID) {
        // Ensure user owns items
        GameObject item = objects.get(itemID);
        if(item == null) return false;

        ItemHash hash = new ItemHash(item);
        return addItemHashShortcutSend(position, hash);
    }

    public boolean removeItemShortcutSend(int position) {
        if(_itemShortcuts.remove(position) != null) {
            // GOOD
            send("OrR"+position);
            return true;
        }
        return false;
    }

    public Optional<Integer> removeItemShortcutByHash(ItemHash hash) {
        Optional<Integer> position = _itemShortcuts.entrySet().stream().filter(e -> hash.equals(e.getValue()))
                            .map(Entry::getKey).findFirst();
        position.ifPresent(_itemShortcuts::remove);
        return position;
    }

    public void sendItemShortcuts() {
        _itemShortcuts.entrySet().stream()
            .map(e -> new Pair<>(e.getKey(), e.getValue()))
            .filter(e -> e.getSecond() != null)
            .map(e ->  "OrA"+ String.join(";",
                String.valueOf(e.first),
                String.valueOf(e.second.templateId),
                e.second.strStats)
            ).forEach(this::send);
    }
}
