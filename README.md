# ExperienceOrb
ExperienceOrb is a RunicExtension supposed to be built-in with the KitPvP Extension. But was coded in its own project folder so I could test it seperately.
<br><br>
ExperienceOrb is an floating orb that spawns randomly or on command. There are three different tiers. When the orb is right-clicked depending on which tier orb you've collected you will receive *n* experience for it.<br>
This experience would go towards your total KitPvP level and could help you level up your kits.

# How it works
ExperienceOrb works by summoning armorstands with a specifier head with different texture for each tiers. And by using a Bukkit Runnable every tick the plugin moves each armorstand a few blocks higher/lower simulating that it is floating. When this armorstand is right-clicked, the plugin automatically checks if its an armorstand, gets the type, and removes the armorstand. Then giving the playing *n* KitPvP experience. The spawning rate would also be affected by amount of players online, to help prevent players from farming experience.
This experienceorb's experience would also be affected by network/server/player boosters.
<br><br>
This is the only screenshot I have on me right now of it. In the screenshot the nametag was bugged but it was fixed. The nametag now appears above the head, instead of under.

![Screenshot](https://i.imgur.com/fJGxf7u.png)
