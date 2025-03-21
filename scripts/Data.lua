-- Init script for static data VM

---@type table<fun>
POST_INITS = {}

-- Load constant data
requireReload("data/AdminCommands")
requireReload("data/AdminGroups")
requireReload("./data/Breed")
requireReload("data/Experience")
requireReload("data/FightTypes")
requireReload("data/GearSlots")
requireReload("data/InteractiveObjects")
requireReload("data/Jobs")
requireReload("data/ObjectiveTypes")
requireReload("data/Animations")
requireReload("data/Skills")

-- Define classes
requireReload("models/NPC")
requireReload("models/MapDef")
requireReload("models/Quest")
requireReload("models/InteractiveObjectDef")

-- Load instances
loadPack("data/objects")
loadPack("data/npcs")
loadPack("data/maps")
loadPack("data/quests")
loadPack("data/skills")
loadPack("data/dungeons") -- Always load after maps

-- Load event handlers
loadPack("eventhandlers")


-- Register Maps to Java
for _, map in pairs(MAPS) do
    RegisterMapDef(map)
end

-- Run POST_INITS handlers
for _, fn in ipairs(POST_INITS) do
    fn()
end
