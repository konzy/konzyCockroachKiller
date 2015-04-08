import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.gui.AccountManager;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Bank;
import org.rsbot.script.methods.Equipment;
import org.rsbot.script.methods.Game.Tab;
import org.rsbot.script.methods.GrandExchange.GEItem;
import org.rsbot.script.methods.Players;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.util.Filter;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSGroundItem;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSPlayer;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.script.wrappers.RSWeb;

/*TODO
 * Paint features to implement
 * 	GUI launcher
 * 	look nicer
 *
 * Other
 * 	Summoning
 * 	Magic
 * 	Magic Potions, Ranged Potions
 */

@ScriptManifest(name = "konzy's Cockroach Killer", authors = "konzy", keywords = {"Combat","Cockroach","Charm"}, 
		version = 1.02, description = "Kills Cockroach Soldiers near Edgeville")

		public class konzyCockroachKiller extends Script implements PaintListener, MessageListener , MouseListener, MouseMotionListener 
		{


	final ScriptManifest properties = getClass().getAnnotation(ScriptManifest.class);

	//private final Image versionImage = getImage("http://i1080.photobucket.com/albums/j321/konzy/konzyCockroachKillerVersion.png");




	static final RSArea BANK_AREA = new RSArea(new RSTile(3091,3488), new RSTile(3097, 3500), 0);
	static final RSArea CREVICE_AREA = new RSArea(new RSTile(3075,3461), new RSTile(3079, 3466), 0);
	static final RSArea UPSTAIRS_AREA = new RSArea(new RSTile(3146,4259), new RSTile(3173, 4281), 1);
	static final RSArea ROPE_AREA = new RSArea(new RSTile(3155,4276), new RSTile(3160, 4281), 1);
	static final RSArea STAIRS_AREA =  new RSArea(new RSTile(3169,4270), new RSTile(3177, 4273));
	static final RSArea DOWNSTAIRS_AREA = new RSArea(new RSTile(3170,4229), new RSTile(3196, 4273), 2);

	static final RSTile BANK_TILE = new RSTile(3093,3491);
	static final RSTile CREVICE_TILE = new RSTile(3077,3462);
	static final RSTile ROPE_TILE = new RSTile(3157,4280);
	static final RSTile DOOR_TILE = new RSTile(3160,4273);
	static final RSTile STAIRS_TILE = new RSTile(3172,4272);
	static final RSTile WEB_TILE = new RSTile(3070,3473);

	static final int STAIRS_DOWN = 29671;
	static final int STAIRS_UP = 29672;
	static final int ROPE = 29729;
	static final int CREVICE = 29728;

	static final String CREVICE_ACTION = "Enter Crevice";
	static final String ROPE_ACTION = "Climb Rope";
	static final String STAIRS_DOWN_ACTION = "Climb-down Stairs";
	static final String STAIRS_UP_ACTION = "Climb-up Stairs";
	static final String CANCEL_ACTION = "Cancel";
	static final int[] FREE_WORLDS = {1,3,4,5,7,8,10,11,13,14,16,17,19,20,25,29,30,33,34,35,37,38,41,43,47,49,50,55,61,62,73,74,75,80,81,87,90,93,102,105,106,108,
		118,120,123,134,135,141,149,152,153,154,155,161,165,167,169};

	static final int[] MEMBER_WORLDS = {2,6,9,12,15,18,21,22,23,24,26,27,28,31,32,36,39,40,42,44,45,46,48,51,52,53,54,56,58,59,60,64,65,66,67,68,69,70,71,72,76,77,78,79,
		82,83,84,85,88,89,91,92,96,97,98,99,100,103,104,112,115,116,117,119,121,129,130,131,132,138,142,143,144,145,148,151,
		156,157,158,159,160,164,166,172};

	static final String[] SELECTABLE_FOOD = {"Herring", "Mackerel", "Trout", "Cod", 
		"Pike", "Salmon", "Tuna", "Cake", "Lobster", "Bass", "Swordfish", "Potato with cheese", 
		"Monkfish", "Shark", "Sea turtle", "Manta ray", "Tuna potato", "Rocktail" };

	static final int[] SELECTABLE_FOOD_IDS = {347, 355, 333, 339, 351, 329, 361, 1891, 379, 
		365, 373, 6705, 7946, 6969, 397, 391, 7060, 15272};

	private static final int[] strengthPotions = {161, 159, 157, 2440, 119, 117, 115, 113};

	private static final int[] attackPotions = {149, 147, 145, 2436, 125, 123, 121};

	private static final int[] defencePotions = {167, 165, 163, 2442, 137, 135, 133};

	private static final int[] combatPotions = {9745, 9743, 9741, 9739};

	private static final int VIAL = 229;

	private RSWeb bankWeb;
	private RSWeb creviceWeb;

	private boolean attackPotionsOut;
	private boolean strengthPotionsOut;
	private boolean defencePotionsOut;
	private boolean combatPotionsOut;
	private RSItem carriedFood;
	private boolean run = true;
	private static int COCKROACH = 7160;
	private String status;
	private boolean goDownstairs, goUpstairs, lootBool, XPBool, infoBool, 
	tempLoot, tempXP, tempInfo, startScript;
	private boolean showPaint = true;

	private Timer specialTimer = new Timer(15000);
	private long startXP[] = new long[24];
	private long startTime;
	private int profit;
	private Loot lootArray = new Loot();
	private final Timer twoCount = new Timer(2000);
	private Timer ammoTimer = new Timer(60000);
	private int lootLength = 91;
	private boolean screenshotOnExit;
	private int mouseDelay;
	private int ammoID;

	private boolean threadRun = true;
	private boolean memberAccount;
	private boolean enableSpecial;
	private boolean pickupAmmo = true;
	private int combatArray[] = new int[4];
	private int foodID;
	private int attackLevelStop;
	private int strengthLevelStop;
	private int defenceLevelStop;
	private int attackPotionsToGet;
	private int strengthPotionsToGet;
	private int defencePotionsToGet;
	private int combatPotionsToGet;
	private int worldJumpTimeMs;
	private int floorSwitchTimeMs;
	private RSGroundItem loot[];
	private konzyCockroachKillerGUI gui;

	static final int[] ITEMS_TO_LOOT = new int[]{
		2366, 985, 987, 1631, 443, 561, 560, 563, 1163, 450,
		1319, 1373, 1249, 1201, 1149, 1147, 1113, 18778, 12160, 12163, 
		1249, 1121, 5295, 1373, 1163, 830, 1333, 1247, 1185, 2366, 
		1617, 1619, 573, 2998, 269, 1215, 7936, 20667, 3000
	};


	Timer worldSwitchTimer = new Timer(worldJumpTimeMs);
	Timer floorSwitchTimer = new Timer(floorSwitchTimeMs);

	private int equippedWeapon;
	private final int SGS = 11698;
	boolean verbose = true;
	private final Image COCKROACH_IMAGE = getImage("http://images2.wikia.nocookie.net/__cb20100911140714/runescape/images/thumb/a/a8/Cockroach_soldier.png/200px-Cockroach_soldier.png");
	private final Font ARIAL = new Font("Arial", 0, 13);
	private final Font largeFont = new Font("Arial", 0, 18);
	private final Font smallFont = new Font("Arial", 0, 10);
	
	private double currentVersion; 
	private boolean outOfDate;




	enum STATE {
		WALK_TO_BANK,		//just walk to bank using webwalking
		WALK_TO_CREVICE,	//just walk to crevice using webwalking
		WALK_TO_CREVICE_CLOSER,	//move closer to the crevice
		WALK_TO_STAIRS,		//just walk using getclosest() for both upstairs and downstairs
		WALK_TO_ROPE,		//just walk using getclosest()
		DESCEND_CREVICE,	//go down into crevice
		ASCEND_ROPE,		//go up rope
		DESCEND_STAIRS,		//go downstairs
		ASCEND_STAIRS,		//go upstairs
		DROP_UNWANTED,		//Drop garbage
		EAT_FOOD,			//Eat food
		DEPOSIT,			//Deposit Inventory
		WITHDRAW_FOOD,		//Withdraw food from bank
		WITHDRAW_POTIONS,
		DRINK_POTION,
		FIND_COCKROACH,
		WALK_TO_WEB,
		SWITCH_WORLDS,
		PICKUP_AMMO,
		REEQUIP_AMMO,
		CHANGE_ATTACK_STYLE,
		SWITCH_STYLE,		//Switch Attack style
		ATTACK,				//Attack Cockroach
		LOOT,				//Pick it up!
		SPECIAL,			//HADUKEN!!!
		RANDOM,				//Detects if in a random
		FIGHTING,
	}

	private STATE getEnum(){
		try {
			if(game.inRandom()
					|| (interfaces.getComponent(906,231).isValid() &&
							interfaces.getComponent(906,236).isValid() && 
							interfaces.getComponent(906,236).containsText("Back") &&
							interfaces.getComponent(906,231).containsAction("Back"))
							|| (interfaces.getComponent(906,160).isValid() && 
									interfaces.getComponent(906,173).isValid() && 
									interfaces.getComponent(906,173).containsText("Play") &&
									interfaces.getComponent(906,160).containsAction("Play")) || 
									game.isLoginScreen() || 
									game.isWelcomeScreen() || 
									!game.isLoggedIn()){
				status = "Random";
				return STATE.RANDOM;
			}
		} catch (Exception e) {}

		if(!worldSwitchTimer.isRunning() && !getMyPlayer().isInCombat()){
			status = "World Jumping";
			return STATE.SWITCH_WORLDS;
		}

		if(!walking.isRunEnabled() && walking.getEnergy() > random(35, 50)){
			walking.setRun(true);
			sleep(500);
		}

		if(inventory.getCount(foodID) == 0){
			status = "Out of Food";
			switch(game.getPlane()){
			case 0:
			{
				RSObject booth = objects.getNearest(Bank.BANK_BOOTHS);
				if(booth != null && booth.isOnScreen()){
					if (!bank.isOpen()){
						status = "Bank - Depositing";
						return STATE.DEPOSIT;
					}else if(((attackPotionsToGet != inventory.getCount(attackPotions) && !attackPotionsOut) ||
							(strengthPotionsToGet != inventory.getCount(strengthPotions) && !strengthPotionsOut) ||
							(defencePotionsToGet != inventory.getCount(defencePotions) && !defencePotionsOut) ||
							(combatPotionsToGet != inventory.getCount(combatPotions)&& !combatPotionsOut))){
						status = "Bank - Withdrawing Potions";
						return STATE.WITHDRAW_POTIONS;

					}else{
						status = "Bank - Withdrawing Food";
						return STATE.WITHDRAW_FOOD;
					}
				}else{
					status = "Bank - Walking to Bank";
					return STATE.WALK_TO_BANK;
				}
			}
			case 3:
			{
				status = "Out of Food - Walking to Rope";
				RSObject rope = objects.getNearest(ROPE);
				if(rope != null && rope.isOnScreen())
					return STATE.ASCEND_ROPE;
				else
					return STATE.WALK_TO_ROPE;
			}
			case 2:
			{
				status = "Out of Food - Walking to Stairs";
				RSObject stairs = objects.getNearest(STAIRS_UP);
				if(stairs != null && stairs.isOnScreen())
					return STATE.ASCEND_STAIRS;
				else
					return STATE.WALK_TO_STAIRS;
			}
			}
		}

		if(getMyPlayer().getHPPercent() < 66){
			status = "Eating Food, OM NOM NOM";
			return STATE.EAT_FOOD;
		}

		switch(game.getPlane()){
		case 0:
		{
			RSObject crevice = objects.getNearest(CREVICE);
			if (crevice == null || (!crevice.isOnScreen() && calc.distanceTo(crevice) > 15)){
				status = "TO BATTLE! - Walking to Crevice";
				return STATE.WALK_TO_CREVICE;
			}else if(!crevice.isOnScreen()){
				status = "TO BATTLE! - Walking to Crevice - Getting Closer";
				return STATE.WALK_TO_CREVICE_CLOSER;
			}else{
				status = "TO BATTLE! - Going into Crevice";
				return STATE.DESCEND_CREVICE;
			}
		}
		case 3:
		{
			goUpstairs = false;
			if(npcs.getNearest(COCKROACH) == null)
				goDownstairs = true;
			if(goDownstairs){
				status = "Going Downstairs";
				RSObject o = objects.getNearest(STAIRS_DOWN);
				if(o == null || !o.isOnScreen())
					return STATE.WALK_TO_STAIRS;
				else
					return STATE.DESCEND_STAIRS;
			}else
				break;
		}
		case 2:
		{
			goDownstairs = false;
			if(goUpstairs){
				status = "Going Upstairs";
				RSObject o = objects.getNearest(STAIRS_UP);
				if(o == null || !o.isOnScreen())
					return STATE.WALK_TO_STAIRS;
				else
					return STATE.ASCEND_STAIRS;
			}
			if(npcs.getNearest(COCKROACH) == null){
				status = "Finding a Roach";
				return STATE.FIND_COCKROACH;
			}
			break;
		}
		}
		if(pickupAmmo){
			if(game.getTab() != Tab.INVENTORY)
				game.openTab(Tab.INVENTORY);
			if(inventory.contains(ammoID)){
				status = "Reequipping Ammo";
				return STATE.REEQUIP_AMMO;
			}
		}

		lootArray.addGroundItems(2);
		loot = groundItems.getAll(LOOT_FILTER);
		for(int i = 0; i < loot.length; i++){
			if(loot != null){
				int lootID = loot[i].getItem().getID();
				if((pickupAmmo && ammoID == lootID) &&
						(!ammoTimer.isRunning() ||
								(!getMyPlayer().isInCombat() && 
										getMyPlayer().getInteracting() == null))){
					status = "Picking up Arrows";
					return STATE.PICKUP_AMMO;
				}else if(lootID != ammoID){
					status = "Looting";
					return STATE.LOOT;
				}
			}
		}


		int fightMode = combat.getFightMode();
		if((fightMode != combatArray[0] && 
				attackLevelStop > skills.getRealLevel(Skills.ATTACK)) ||
				(strengthLevelStop > skills.getRealLevel(Skills.STRENGTH) && 
						fightMode != combatArray[1] && 
						attackLevelStop <= skills.getRealLevel(Skills.ATTACK)) || 
						(fightMode != combatArray[3] &&
								defenceLevelStop > skills.getRealLevel(Skills.DEFENSE) &&
								attackLevelStop <= skills.getRealLevel(Skills.ATTACK)) &&
								strengthLevelStop <= skills.getRealLevel(Skills.STRENGTH)){
			return STATE.CHANGE_ATTACK_STYLE;

		}


		if (!getMyPlayer().isInCombat() && 
				getMyPlayer().getInteracting() == null) {
			status = "Attacking";
			return STATE.ATTACK;
		}
		if(!specialTimer.isRunning() &&
				enableSpecial &&
				(equippedWeapon != SGS ||
						(equippedWeapon == SGS  && 
								(skills.getRealLevel(Skills.CONSTITUTION) * 10 - combat.getLifePoints() > 200))) &&
								(combat.getSpecialBarEnergy()) == 100){
			status = "Special Attack";
			return STATE.SPECIAL;
		}

		if((inventory.getCount(attackPotions) > 0 && 
				skills.getCurrentLevel(Skills.ATTACK) == skills.getRealLevel(Skills.ATTACK)) ||
				(inventory.getCount(strengthPotions) > 0 && 
						skills.getCurrentLevel(Skills.STRENGTH) == skills.getRealLevel(Skills.STRENGTH)) ||
						(inventory.getCount(defencePotions) > 0 && 
								skills.getCurrentLevel(Skills.DEFENSE) == skills.getRealLevel(Skills.DEFENSE)) ||
								(inventory.getCount(combatPotions) > 0 && 
										skills.getCurrentLevel(Skills.ATTACK) == skills.getRealLevel(Skills.ATTACK) &&
										skills.getCurrentLevel(Skills.STRENGTH) == skills.getRealLevel(Skills.STRENGTH) &&
										skills.getCurrentLevel(Skills.DEFENSE) == skills.getRealLevel(Skills.DEFENSE))){
			status = "Drinking Potion";
			return STATE.DRINK_POTION;
		}

		status = "Fighting";
		return STATE.FIGHTING;

	}
	
	
	public boolean onStart(){
		status = "Starting Up";

		new VersionChecker();

		memberAccount = AccountManager.isMember(account.getName());

		startTime = (int) System.currentTimeMillis();
		env.disableRandoms();
		worldTimer();
		startScript = false;
		if(!lootArray.load()){
			lootArray = new Loot(1,ITEMS_TO_LOOT);
			lootArray.updatePrices();

			int i = 0;
			while(lootArray.threadIsRunning()){
				if(i % 4 == 0)
					log("First Run - Getting Prices and Names.");
				else if(i % 4 == 1)
					log("First Run - Getting Prices and Names..");
				else if(i % 4 == 2)
					log("First Run - Getting Prices and Names...");
				else
					log("First Run - Getting Prices and Names....");
				i++;
				sleep(10000);
			}
		}
		lootArray.save();
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					gui = new  konzyCockroachKillerGUI();
				}
			});
		} catch (Throwable e) {
		}
		gui.setVisible(true);
		while (gui.isVisible())
			sleep(100);
		gui.dispose();
		if(!startScript)
			return false;
		
		
		new PriceCheckThread().start();

		return true;
	}


	public void onFinish(){
		if (screenshotOnExit) {
			{

				infoBool = true;
				XPBool = true;
				lootBool = true;
				sleep(100);
				env.saveScreenshot(true);	
				log("Screenshot taken!");
			}
			sleep(500);
		}
		run = false;
	}
	
	private class VersionChecker extends Thread{
		public void run(){
			check();
			Timer fiveMin = new Timer(300000);
			while(threadRun){
				if(!fiveMin.isRunning()){
					check();
					fiveMin.reset();
				}
			}
		}
		private void check(){
			try {
				URLConnection url = new URL("http://scripters.powerbot.org/files/273499/konzyCockroachKillerVERSION.txt").openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(url.getInputStream()));
				currentVersion = Double.parseDouble(in.readLine());
				if (currentVersion > properties.version()) {
					outOfDate = true;
				}

			} catch (IOException e) {
				log("Problem getting version :/");
			}
		}
	}

	private void mouseOverCombat(int component){
		if(game.getTab() != Tab.ATTACK)
			game.openTab(Tab.ATTACK);
		mouse.move(interfaces.getComponent(884, component).getCenter(),15,15);
		twoCount.reset();
		while(interfaces.getComponent(884, 19).getComponent(2) == null && twoCount.isRunning())
			sleep(random(250,500));
	}

	public int loop(){

		mouse.setSpeed(random(4 + mouseDelay, 7 + mouseDelay));

		if(equippedWeapon == 0  && game.isLoggedIn() && getMyPlayer().isOnScreen()){
			status = "Starting Up";
			equippedWeapon = equipment.getItem(Equipment.WEAPON).getID();
			getSkills();
			getAmmoInfo();
			if(ammoID <= 0)
				pickupAmmo = false;

			for(int i = 0; i < 4; i++){
				mouseOverCombat(11 + i);
				if(interfaces.getComponent(884, 19).getComponent(2) != null){
					if(interfaces.getComponent(884, 19).getComponent(2).containsText("Attack"))
						combatArray[0] = i;
					else if(interfaces.getComponent(884, 19).getComponent(2).containsText("Strength"))
						combatArray[1] = i;
					else if(interfaces.getComponent(884, 19).getComponent(2).containsText("Controlled"))
						combatArray[2] = i;
					else if(interfaces.getComponent(884, 19).getComponent(2).containsText("Defence"))
						combatArray[3] = i;
					else if(interfaces.getComponent(884, 19).getComponent(2).containsText("Rapid")){
						combatArray[0] = i;
						combatArray[1] = i;
						combatArray[2] = i;
						combatArray[3] = i;
					}
				}
			}	
		}

		if(!game.inRandom() || !game.isLoginScreen() || 
				!game.isWelcomeScreen() || interfaces.getComponent(906, 231).isValid()){
			env.disableRandoms();
		}
		if(game.inRandom() && 
				!interfaces.getComponent(906, 231).isValid() && 
				!interfaces.getComponent(906, 173).isValid()){
			env.enableRandoms();
			return 100;
		}
		if(inventory.contains(VIAL)){
			twoCount.reset();
			inventory.getItem(VIAL).interact("Drop");
			while(inventory.contains(VIAL) && twoCount.isRunning()){
				sleep(100);
			}
			return random(1000, 1500);
		}
		switch (getEnum()){
		case RANDOM:
		{
			env.disableRandoms();
			if((interfaces.getComponent(906,236).isValid() &&
					interfaces.getComponent(906,236).containsText("Back") &&
					interfaces.getComponent(906,231).isValid() &&
					interfaces.getComponent(906,231).containsAction("Back"))){
				interfaces.getComponent(906,231).doClick();
				return 1000;
			}
			if((interfaces.getComponent(906,173).isValid() &&
					interfaces.getComponent(906,173).containsText("Play") &&
					interfaces.getComponent(906,160).isValid() &&
					interfaces.getComponent(906,160).containsAction("Play"))){
				interfaces.getComponent(906,160).doClick();
				return 1000;
			}
			env.enableRandoms();
			return 1000;
		}
		case EAT_FOOD:
		{
			eatFood();
			return random(2000, 3000);
		}
		case WALK_TO_BANK:
		{
			closeWindows();
			if (bankWeb == null) {
				bankWeb = web.getWeb(getMyPlayer().getLocation(), BANK_TILE);
				if(bankWeb == null)
					bankWeb = web.getWeb(WEB_TILE, BANK_TILE);
			}
			try {
				bankWeb.step();
			} catch (Exception ignored) {
				bankWeb = null;
				return 100;}
			if(bankWeb.finished())
				bankWeb = null;
			return random(1000, 3000);

		}
		case DEPOSIT:
		{
			if(!bank.isOpen())
				bank.open();
			for(int i = 0; i < 50 && !bank.isOpen(); i++)
				sleep(100);
			if(inventory.getCount() > 0)
				bank.depositAll();
			twoCount.reset();
			while(inventory.getCount() > 0 && twoCount.isRunning())
				sleep(100);
			return random(1000, 2000);
		}
		case WITHDRAW_POTIONS:
		{
			if(bank.getCount(attackPotions) == 0)
				attackPotionsOut = true;
			if(bank.getCount(strengthPotions) == 0)
				strengthPotionsOut = true;
			if(bank.getCount(defencePotions) == 0)
				defencePotionsOut = true;
			if(bank.getCount(combatPotions) == 0)
				combatPotionsOut = true;
			if(!attackPotionsOut || !strengthPotionsOut || !defencePotionsOut || !combatPotionsOut){
				if(attackPotionsToGet > 0 && !attackPotionsOut){
					for(int potion:attackPotions){
						if(bank.getItem(potion) != null && inventory.getCount(attackPotions) < attackPotionsToGet){
							bank.withdraw(potion, attackPotionsToGet - inventory.getCount(attackPotions));
							twoCount.reset();
							while(inventory.getCount(potion) < attackPotionsToGet && twoCount.isRunning())
								sleep(100);
						}
					}
				}
				if(strengthPotionsToGet > 0 && !strengthPotionsOut){
					for(int potion:strengthPotions){
						if(bank.getItem(potion) != null && inventory.getCount(strengthPotions) < strengthPotionsToGet){
							bank.withdraw(potion, strengthPotionsToGet - inventory.getCount(strengthPotions));
							twoCount.reset();
							while(inventory.getCount(potion) < strengthPotionsToGet && twoCount.isRunning())
								sleep(100);
						}
					}
				}
				if(defencePotionsToGet > 0 && !defencePotionsOut){
					for(int potion:defencePotions){
						if(bank.getItem(potion) != null && inventory.getCount(defencePotions) < defencePotionsToGet){
							bank.withdraw(potion, defencePotionsToGet - inventory.getCount(defencePotions));
							twoCount.reset();
							while(inventory.getCount(potion) < defencePotionsToGet && twoCount.isRunning())
								sleep(100);
						}
					}
				}
				if(combatPotionsToGet > 0 && !combatPotionsOut){
					for(int potion:combatPotions){
						if(bank.getItem(potion) != null && inventory.getCount(combatPotions) < combatPotionsToGet){
							bank.withdraw(potion, combatPotionsToGet - inventory.getCount(combatPotions));
							twoCount.reset();
							while(inventory.getCount(potion) < combatPotionsToGet && twoCount.isRunning())
								sleep(100);
						}
					}
				}
			}

			return random(1000, 2000);
		}
		case WITHDRAW_FOOD:
		{
			if(bank.getCount(foodID) == 0)
				outOfFood();
			bank.withdraw(foodID, 0);
			if(bank.isOpen())
				bank.close();
			twoCount.reset();
			while(bank.isOpen() && twoCount.isRunning())
				sleep(100);
			return random(1000, 2000);
		}
		case WALK_TO_CREVICE:
		{
			closeWindows();
			if (creviceWeb == null) {
				creviceWeb = web.getWeb(getMyPlayer().getLocation(), CREVICE_TILE);
				if(creviceWeb == null)
					creviceWeb = web.getWeb(BANK_TILE, WEB_TILE);
			}
			try {
				creviceWeb.step();
			} catch (Exception ignored) {
				creviceWeb = null;
				return 100;}
			if(creviceWeb.finished())
				creviceWeb = null;
			return random(1000, 3000);
		}
		case WALK_TO_CREVICE_CLOSER:
		{
			walking.walkTileMM(objects.getNearest(CREVICE).getLocation(), 2, 2);
			return random(1000, 3000);
		}

		case DESCEND_CREVICE:
		{
			climb(objects.getNearest(CREVICE), CREVICE_ACTION);
			return random(2000, 3000);
		}
		case ASCEND_ROPE:
		{
			climb(objects.getNearest(ROPE), ROPE_ACTION);
			return random(500, 1000);
		}
		case DESCEND_STAIRS:
		{
			climb(objects.getNearest(STAIRS_DOWN), STAIRS_DOWN_ACTION);
			return random(2000, 3000);
		}
		case ASCEND_STAIRS:
		{
			climb(objects.getNearest(STAIRS_UP), STAIRS_UP_ACTION);
			return random(2000, 3000);
		}
		case WALK_TO_STAIRS:
		{
			RSTile player = getMyPlayer().getLocation();
			RSTile t = closestReachableTile(STAIRS_TILE);
			walking.walkTileMM(t, 2, 2);
			sleep(random(500, 1000));
			if(player == getMyPlayer().getLocation()){
				walking.walkTileOnScreen(t);
				log("problem walking to stairs, trying to fix it");	
			}
			return random(500, 1000);
		}
		case WALK_TO_ROPE:
		{

			if(walking.getClosestTileOnMap(ROPE_TILE) != ROPE_TILE)
				walking.walkTileMM(DOOR_TILE, 2, 2);
			else walking.walkTileMM(ROPE_TILE, 2, 2);
			return random(500,1000);
		}
		case FIND_COCKROACH:
		{
			if(!getMyPlayer().isMoving()){
				RSTile downstairsTiles[] = DOWNSTAIRS_AREA.getTileArray();
				walking.walkTileMM(walking.getClosestTileOnMap(
						downstairsTiles[random(0, downstairsTiles.length)]), 2, 2);
			}
			return random(500, 1000);
		}
		case CHANGE_ATTACK_STYLE:
		{
			if(game.getTab() != Tab.ATTACK)
				game.openTab(Tab.ATTACK);


			if(attackLevelStop > skills.getRealLevel(Skills.ATTACK))
				combat.setFightMode(combatArray[0]);
			else if(strengthLevelStop > skills.getRealLevel(Skills.STRENGTH))
				combat.setFightMode(combatArray[1]);
			else if(defenceLevelStop > skills.getRealLevel(Skills.DEFENSE))
				combat.setFightMode(combatArray[3]);

		}
		case LOOT:
		{		
			for(int i = 0; i < loot.length; i++){

				if(!getMyPlayer().isInCombat()){
					RSTile lootLoc = loot[i].getLocation();
					int lootID = loot[i].getItem().getID();
					int amount = loot[i].getItem().getStackSize();
					int priority = lootArray.getPriority(lootID);
					String lootName = loot[i].getItem().getName();
					boolean stacks = amount > 1 || 
					lootArray.isNoted(lootID) || 
					lootArray.isStackable(lootID) ||
					(inventory.contains(lootID) && 
							inventory.getItem(lootID).getStackSize() > 1);

					if(stacks != lootArray.isStackable(lootID))
						lootArray.setStackable(lootID, stacks);

					if(lootArray.getName(lootID) != lootName){
						lootArray.setName(lootID, lootName);
					}
					status = "Looting - " + lootName;

					if ((priority == 1 && inventory.isFull()) &&
							(!stacks || !inventory.contains(lootID))){
						eatFood();
					}
					if (!loot[i].isOnScreen()){
						new CameraThread().run(lootLoc);
						if(walking.getClosestTileOnMap(lootLoc) == lootLoc){
							walking.walkTileMM(lootLoc, 2, 2);
							while(!loot[i].isOnScreen()){
								sleep(random(500, 1000));
								walking.walkTileMM(lootLoc, 2, 2);
							}
						}
						else
							continue;
					}
					twoCount.reset();
					while(!loot[i].interact("Take " + lootName) && twoCount.isRunning())
						sleep(random(250, 500));
					twoCount.reset();
					int inv = inventory.getCount(true, lootID);
					while(inventory.getCount(true, lootID) == inv && twoCount.isRunning())
						sleep(100);
					if(inventory.getCount(true, lootID) != inv){
						lootArray.incrementPickedUp(lootID, amount);
						profit += lootArray.getPrice(lootID) * amount;
					}
					return random(500, 1500);
				}
			}
			return 100;
		}
		case PICKUP_AMMO:
		{
			if(inventory.getCount(ammoID) == 0 && inventory.isFull())
				eatFood();

			for(int i = 0; i < loot.length; i++){
				if(loot[i].getItem().getID() == ammoID)

					if(loot[i].isOnScreen())
						loot[i].interact("Take");
					else{
						twoCount.reset();
						walking.walkTileMM(loot[i].getLocation(), 2, 2);
						while(!loot[i].isOnScreen() && twoCount.isRunning())
							sleep(100);
						if(loot[i].isOnScreen())
							loot[i].interact("Take");
					}
				ammoTimer = new Timer(random(40000, 60000));
				twoCount.reset();
				int inv = inventory.getCount(true, ammoID);
				while(inventory.getCount(true, ammoID) == inv && twoCount.isRunning())
					sleep(100);
			}
		}
		case REEQUIP_AMMO:
		{
			if(inventory.getCount(ammoID) > 0)
				inventory.getItem(ammoID).doClick(true);
			return random(500, 1000);
		}
		case ATTACK:
		{
			if(!combat.isAutoRetaliateEnabled())
				combat.setAutoRetaliate(true);
			RSNPC npc = npcs.getNearest(COCKROACH_FILTER);
			if (npc == null){
				if(game.getPlane() == 3)
					goDownstairs = true;
				return random(250, 750);
			}
			RSTile npcLoc = npc.getLocation();
			if (!npc.isOnScreen()) {
				if(calc.distanceTo(npcLoc) < 8)
					walking.walkTileOnScreen(npcLoc);
				else
					walking.walkTileMM(walking.getClosestTileOnMap(npcLoc), 2, 2);
				new CameraThread().run(npcLoc);
				return random(1500, 2000);
			}else
				npc.interact("Attack " + npc.getName());
			return random(750, 1000);
		}
		case SWITCH_WORLDS:
		{
			sleep(11000);
			int tempWorlds[];
			if(memberAccount)
				tempWorlds = MEMBER_WORLDS;
			else
				tempWorlds = FREE_WORLDS;
			game.switchWorld(tempWorlds[random(0,tempWorlds.length)]);
			worldTimer();
			return(random(5000, 10000));
		}
		case DRINK_POTION:
		{
			int currentAttack = skills.getCurrentLevel(Skills.ATTACK);
			int realAttack = skills.getRealLevel(Skills.ATTACK);
			int currentStrength = skills.getCurrentLevel(Skills.STRENGTH);
			int realStrength = skills.getRealLevel(Skills.STRENGTH);
			int currentDefence = skills.getCurrentLevel(Skills.DEFENSE);
			int realDefence = skills.getRealLevel(Skills.DEFENSE);

			if(inventory.getCount(combatPotions) > 0 && 
					currentAttack == realAttack &&
					currentStrength == realStrength){
				for(int potion:combatPotions){
					if(inventory.contains(potion)){
						inventory.getItem(potion).doClick(true);
						break;
					}
				}
			}
			if((inventory.getCount(attackPotions) > 0 && 
					currentAttack == realAttack)){
				for(int potion:attackPotions){
					if(inventory.contains(potion)){
						inventory.getItem(potion).doClick(true);
						break;
					}
				}
			}
			if((inventory.getCount(strengthPotions) > 0 && 
					currentStrength == realStrength)){
				for(int potion:strengthPotions){
					if(inventory.contains(potion)){
						inventory.getItem(potion).doClick(true);
						break;
					}
				}
			}
			if((inventory.getCount(defencePotions) > 0 && 
					currentDefence == realDefence)){
				for(int potion:defencePotions){
					if(inventory.contains(potion)){
						inventory.getItem(potion).doClick(true);
						break;
					}
				}
			}
			return random(1500, 3000);
		}
		case SPECIAL:
		{
			combat.setSpecialAttack(true);/*
			if(game.getTab() != Tab.ATTACK)
				game.openTab(Tab.ATTACK);
			mouse.click(644,423,68,5,true);*/
			specialTimer = new Timer(random(15000, 30000));
			return random(500, 1000);
		}
		case FIGHTING:
		{
			antiBan();
			return random(500, 1000);
		}
		}
		return random(500, 1000);
	}

	private int antiBan(){
		int randomNo = random(1, 200);
		int r = random(1, 7);
		if (randomNo == 1) {
			switch(r){
			case 1:{
				status = "AB Checking Out Other Players 3s - 5s";
				RSPlayer p = players.getNearest(Players.ALL_FILTER);
				if(p != null)
					p.hover();
				else
					mouse.moveSlightly();
				return random(3000 , 5000);
			}
			case 2:{
				status = "AB Open Random Tab 3s - 5s";
				game.getRandomTab();
				return random(3000 , 5000);
			}
			case 3: {
				status = "AB Move Mouse Slightly .5s - 1s";
				mouse.moveSlightly();
				return random(500 , 1000);
			}
			case 4: {
				status = "AB Move Mouse .5s - 1s";
				mouse.moveRandomly(70, 380);
				return random(500 , 1000);
			}
			case 5: {
				status = "AB Move Off Screen 4s - 5s";
				mouse.moveOffScreen();
				return random(4000 , 5000);
			}
			case 6: {
				status = "AB Move Mouse Slightly .5s - 1s";
				mouse.moveSlightly();
				return random(500 , 1000); 
			}
			case 7: {
				status = "AB Look At Random Combat Skill 5s - 10s";
				if (game.getTab() != Tab.STATS) {
					game.openTab(Tab.STATS);
					sleep(random(500, 700));
				}
				mouse.move(random(545 , 600), random(200, 380));
				return random(5000 , 10000);
			}
			}
		}
		return 100;
	}

	private final Filter<RSGroundItem> LOOT_FILTER = new Filter<RSGroundItem>() {
		@Override
		public boolean accept(RSGroundItem l) {
			RSTile lootLoc = l.getLocation();
			int lootID = l.getItem().getID();
			int stackSize = 1;
			if(inventory.contains(lootID))
				stackSize = inventory.getItem(lootID).getStackSize();
			if(lootID == 229)
				return false;
			if((DOWNSTAIRS_AREA.contains(lootLoc) && game.getPlane() != 2) || 
					(UPSTAIRS_AREA.contains(lootLoc) && game.getPlane() != 3) || 
					calc.distanceTo(lootLoc) > 10 )
				return false;
			if(inventory.isFull() && lootArray.getPriority(lootID) != 1 && lootID != ammoID &&
					(stackSize <= 1 || !lootArray.getStackable(lootID)))
				return false;
			else
				return true;
			
/*			return ((((DOWNSTAIRS_AREA.contains(lootLoc) && game.getPlane() == 2) || 
					(UPSTAIRS_AREA.contains(lootLoc) && game.getPlane() == 3))) &&
					(lootArray.getPriority(lootID) == 1 ||
							(lootArray.getPriority(lootID) == 2 &&
									(!inventory.isFull() || 
											stackSize > 1 ||
											lootArray.getStackable(lootID))) ||
											lootID == ammoID) &&
											calc.distanceTo(lootLoc) < 10);*/
		}
	};

	private void getSkills(){
		for(int i = 0; i < 24; i++)
			startXP[i] = skills.getCurrentExp(i);
	}

	private void getAmmoInfo(){
		if(pickupAmmo){
			ammoID = equipment.getItem(Equipment.AMMO).getID();
		} 
	}



	private final Filter<RSNPC> COCKROACH_FILTER = new Filter<RSNPC>() {
		@Override
		public boolean accept(RSNPC n) {
			return !n.isInCombat()
			&& n.getHPPercent() > 0
			&& getMyPlayer().getInteracting() == null
			&& n.getID() == COCKROACH;
		}
	};

	private void worldTimer(){
		int deviation = 1800000; //30 min
		worldSwitchTimer = new Timer(random(worldJumpTimeMs - deviation, worldJumpTimeMs + deviation));
		//worldSwitchTimer = System.currentTimeMillis() + 60000; //For testing
	}
	private boolean canReach(RSTile t){
		try {
			return calc.canReach(t, false) || calc.canReach(t, true);
		} catch (Exception e) {}
		return false;
	}

	private RSTile closestReachableTile(RSTile destination){
		RSTile nextStep = walking.getClosestTileOnMap(destination);
		if(nextStep == destination)
			return destination;
		for(int i = 1; i < 20; i++){
			if (i % 2 == 1) {
				for (int j = 0; j < i; j++) {
					nextStep = new RSTile(nextStep.getX(), nextStep.getY() + 1);
					if (canReach(nextStep))
						return nextStep;
				}
				for (int j = 0; j < i; j++) {
					nextStep = new RSTile(nextStep.getX() + 1, nextStep.getY());
					if(canReach(nextStep))
						return nextStep;
				}
			}else{
				for (int j = 0; j < i; j++) {
					nextStep = new RSTile(nextStep.getX(), nextStep.getY() - 1);
					if(canReach(nextStep))
						return nextStep;
				}
				for (int j = 0; j < i; j++) {
					nextStep = new RSTile(nextStep.getX() - 1, nextStep.getY());
					if(canReach(nextStep))
						return nextStep;
				}
			}
		}
		return nextStep;


	}

	private void closeWindows(){
		try {
			if(store.isOpen()){
				store.close();
				int i = 0;
				while(store.isOpen() && i < 50){
					sleep(100);
					i++;
				}
			}
			if(bank.isOpen()){
				bank.close();
				int i = 0;
				while(bank.isOpen() && i < 50){
					sleep(100);
					i++;
				}
			}
			if (interfaces.canContinue()){
				interfaces.clickContinue();
				int i = 0;
				while(interfaces.getContinueComponent().isValid() && i < 50){
					sleep(100);
					i++;
				}
			}
		} catch (Exception e) {
			if(verbose)
				log("Caught Exception in closeWindows()");}
	}

	/*	private boolean withdrawFromBank(int item, int count, String name){
		if(bank.getCount(item) < count){
			log("No enough " + name + " left");
			return false;
		}

		int i = 0;
		int tempCount = count;
		int withdrawAmount = 0;
		int start = inventory.getCount(item);
		while(i < 25){
			if(tempCount >= 10)
				withdrawAmount = 10;
			else if(tempCount >= 5)
				withdrawAmount = 5;
			else if(tempCount >= 1)
				withdrawAmount = 1;
			bank.withdraw(item, withdrawAmount);
			twoCount.reset();
			while ((inventory.getCount(item) != (start + withdrawAmount) || !inventory.isFull())
					&& twoCount.isRunning())
				if(inventory.getCount(item) == start + withdrawAmount)
					tempCount -= withdrawAmount;
			if(inventory.getCount(item) == count || inventory.isFull())
				return true;

			i++;
		}
		log("Could not get all of" + name + " after a 25 tries");
		return false;
	}*/
	private void outOfFood(){
		log("Could not find any food in your bank, check your tabs, or the food ID");
		log("Out of food! logging out...");
		sleep(random(4000, 5000));
		bank.close();
		game.logout(true);
		stopScript(true);
	}

	private boolean eatFood() {
		carriedFood = getFood();
		for(int i = 0; i < 3; i++) {
			if(!inventory.contains(foodID)){
				break;
			}
			int foodCount = inventory.getCount(foodID);
			Timer t = new Timer(200);
			try{
				if(carriedFood.getComponent().containsText("Drink")){
					if(carriedFood.interact("Drink")) {  
						while(foodCount == inventory.getCount(foodID) && t.isRunning())
							sleep(100);
						return true;
					}
				}else if(carriedFood.interact("Eat")) {  
					while(foodCount == inventory.getCount(foodID) && t.isRunning())
						sleep(100);
					return true;
				}
			}catch(Exception e){}
		}
		/*		if (getCurrentLifepoint() <= bailHP) {
			log.warning("HP less than" + bailHP + "Logging out.");
			game.logout(true);
			stopScript(true);
		}*/
		return false;
	}
	private RSItem getFood(){
		for(RSItem i : inventory.getItems()) {
			if(i == null || i.getID() == -1)
				continue;
			if (i.getComponent().getActions() == null || i.getComponent().getActions()[0] == null)
				continue;
			if (i.getComponent().getActions()[0].contains("Eat")){
				return i;
			}  
		}
		return null;
	}

	private class PriceCheckThread extends Thread {
		public void run() {
			Timer t = new Timer(random(10800000, 43200000));
			while(run){
				if(!t.isRunning()){
					lootArray.updatePrices();
					lootArray.update();
					lootArray.save();
					t = new Timer(random(10800000, 43200000));
				}
			}
		}
	}

	private class CameraThread extends Thread {
		public void run(RSTile t) {
			camera.turnTo(t, 15);
		}
	}

	private void climb(RSObject o, String s){
		if(o != null){
			sleep(50);
			if(o.isOnScreen()){
				o.doClick(false);
				if(menu.contains(s))
					menu.doAction(s);
				else
					menu.doAction(CANCEL_ACTION);
			}else{
				walking.walkTileMM(o.getLocation(), 1, 1);
			}
		}
	}

	class Loot{

		private static final int COINS = 995;
		final int[] noValue = {18778, 12160, 12163, 12159, 12158, 995, 14664};
		final String[] noValueName = {"Starved Ancient Effigy", "Crimson Charm", "Blue Charm",
				"Green Charm", "Gold Charm", "Coins", "Random Event Gift"};
		private ArrayList<String> name = new ArrayList<String>(50);
		private ArrayList<Integer> ID = new ArrayList<Integer>(50);
		private ArrayList<Integer> price = new ArrayList<Integer>(50);
		private ArrayList<Integer> priority = new ArrayList<Integer>(50);
		private ArrayList<Boolean> noted = new ArrayList<Boolean>(50);
		private ArrayList<Boolean> stackable = new ArrayList<Boolean>(50);
		private ArrayList<GEItem> geItem = new ArrayList<GEItem>(50);
		private ArrayList<Integer> pickedUpAmounts = new ArrayList<Integer>(50);
		private ArrayList<Integer> pickedUpIDS = new ArrayList<Integer>(50);
		private ArrayList<InitThread> initThread = new ArrayList<InitThread>(50);
		private final File itemFile = new File(getCacheDirectory() +
				System.getProperty("file.separator") + "lootItems.ini");


		public Loot() {}
		public Loot(int... id) {
			for(int i: id){
				add(i);
			}
		}
		public Loot(int priority, int... id) {
			for(int i: id){
				add(priority, i);
			}
		}
		public Loot(int priority, boolean noted, int... id) {
			for(int i: id){
				add(priority, noted, i);
			}
		}
		public Loot(int[] priority, boolean[] noted, boolean[] stackable, int[] id, int[] price, String[] name) {
			for(int i = 0; i < priority.length; i++){
				add(priority[i], noted[i], stackable[i], id[i], price[i], name[i]);
			}
		}
		public void add(int id){
			add(0, id);
		}
		public void add(int priority, int id){
			add(priority, false, id);
		}
		public void add(int priority, boolean noted, int id){
			add(priority, noted, false, id);
		}
		public void add(int priority, boolean noted, boolean stackable, int id){
			add(priority, noted, stackable, id, 0, "");
		}
		public void add(int priority, boolean noted, boolean stackable, int id, int price, String name){
			this.ID.add(id);
			this.noted.add(noted);
			this.stackable.add(stackable);
			this.priority.add(priority);
			this.price.add(price);
			this.name.add(name);
			this.geItem.add(null);
			InitThread t = new InitThread();
			t.setID(id);
			initThread.add(t);
		}		
		public void add(RSItem item){
			add(0, item);
		}
		public void add(int priority, RSItem item){
			int id = item.getID();
			add(priority, false, item.getStackSize() > 1, id, 0, item.getName());
		}
		/**
		 * Gets all ground items and adds items to Loot then
		 * starts a new thread to get price.
		 *
		 * @param priority Sets the priority of new items picked up.
		 * 
		 * @return If a new item was added to the Loot.
		 */
		public void addGroundItems(int priority){
			RSGroundItem loot[] = groundItems.getAll();
			boolean added = false;
			if(loot != null){
				for(RSGroundItem l:loot){
					boolean add = true;
					int groundItemID = l.getItem().getID();
					for(int lootArrayID: getIDS()){
						if(lootArrayID == groundItemID){
							add = false;
							break;
						}
					}
					if (add) {
						added = true;
						if(l.getItem().getStackSize() > 1)
							add(2, false, true, groundItemID, 0, l.getItem()
									.getName());
						else
							add(2, false, false, groundItemID, 0, l.getItem()
									.getName());
					}
				}
			}
			if(added)
				save();
		}
		/**
		 * Gets all ground items and adds items to Loot then
		 * starts a new thread to get price
		 * 
		 * @return If a new item was added to the Loot.
		 */
		public void addGroundItems(){
			addGroundItems(0);
		}
		public boolean contains(int id){
			return this.ID.contains(id);
		}
		public boolean contains(String name){
			return this.name.contains(name);
		}
		public int getPriority(int id){
			return priority.get(ID.indexOf(id));
		}
		public String getName(int id) {
			String name = this.name.get(ID.indexOf(id));
			if(name == null || name.equals(""))
				return Integer.toString(id);
			return name;
		}
		public String[] getNames(){
			return (String[]) this.name.toArray();
		}
		public int getPrice(int id) {
			return price.get(ID.indexOf(id));
		}
		public int getID(int index) {
			return ID.get(index);
		}
		public GEItem getGEItem(int id) {
			return geItem.get(ID.indexOf(id));
		}
		public boolean getStackable(int id) {
			return stackable.get(ID.indexOf(id));
		}
		public boolean isNoted(int id){
			return noted.get(ID.indexOf(id));
		}
		public boolean isStackable(int id){
			return stackable.get(ID.indexOf(id));
		}
		public void setStackable(int id, boolean stackable){
			this.stackable.set(ID.indexOf(id), stackable);
		}
		public void updatePrices(){
			if(!threadIsRunning())
				for(InitThread thread:initThread){
					thread.start();
				}
		}
		public int[] getIDS() {
			int index = 0;
			int[] array = new int[ID.toArray().length];
			for (Object o : ID.toArray()) {
				array[index++] = (Integer)o;
			}
			return array;
		}
		public int getSize() {
			return ID.size();
		}
		public boolean threadIsRunning() {
			for(InitThread thread:initThread){
				if(thread.isAlive())
					return true;
			}
			return false;
		}
		public int getIndexOfID(int id){
			return ID.indexOf(id);
		}
		public void setName(int id, String name) {
			int index = ID.indexOf(id);
			this.name.set(index, name);
		}
		public void setPriority(int id, int priority) {
			int index = ID.indexOf(id);
			this.priority.set(index, priority);
		}
		public int[] getPickedUpIDS(){
			int size = pickedUpIDS.size();
			int[] array = new int[size];
			for(int i = 0; i < size; i++){
				array[i] = pickedUpIDS.get(i);
			}
			return array;
		}
		public int pickedUpSize(){
			return pickedUpIDS.size();
		}
		public int[] getPickedUpAmounts(){
			int size = pickedUpAmounts.size();
			int[] array = new int[size];
			for(int i = 0; i < size; i++){
				array[i] = pickedUpAmounts.get(i);
			}
			return array;
		}
		public int getPickedUpAmount(int id){
			return pickedUpAmounts.get(pickedUpIDS.indexOf(id));
		}
		public void incrementPickedUp(int id, int amount) {
			if(!pickedUpIDS.contains(id)){
				pickedUpIDS.add(id);
				pickedUpAmounts.add(amount);
			}else{
				int index = pickedUpIDS.indexOf(id);
				pickedUpAmounts.set(index, pickedUpAmounts.get(index) + amount);
			}
		}

		public boolean load() {
			Properties p = new Properties();
			try {
				if(!itemFile.exists()){
					itemFile.createNewFile();
					return false;
				}
				p.load(new FileInputStream(itemFile));
			} catch (Exception e) {log("load item properties failed");}

			if(p.getProperty("itemSize") != null){
				int originalSize = ID.size();
				int savedSize = Integer.parseInt(p.getProperty("itemSize"));
				int priority;
				int id;
				int price;
				boolean noted;
				boolean stackable;
				String name;
				for (int i = 0; i < savedSize; i++) {
					if(p.getProperty("itemID" + i) != null)
						id = Integer.parseInt(p.getProperty("itemID" + i));
					else
						id = 0;
					if(p.getProperty("itemPriority" + i) != null)
						priority = Integer.parseInt(p.getProperty("itemPriority" + i));
					else
						priority = 0;
					if(p.getProperty("itemPrice" + i) != null)
						price = Integer.parseInt(p.getProperty("itemPrice" + i));
					else
						price = 0;
					if(p.getProperty("itemName" + i) != null)
						name = p.getProperty("itemName" + i);
					else
						name = "";
					noted = p.getProperty("itemNoted" + i) != null && p.getProperty("itemNoted" + i).equals("true");
					stackable = p.getProperty("itemStackable" + i) != null && p.getProperty("itemStackable" + i).equals("true");
					if(i >= originalSize)
						add(priority, noted, stackable, id, price, name);
					else{
						int index = ID.indexOf(id);
						this.noted.set(index, noted);
						this.stackable.set(index, stackable);
						this.priority.set(index, priority);
						this.price.set(index, price);
						this.name.set(index, name);
					}
				}
			}else
				return false;

			log("items loaded");
			return true;
		}

		public void save() {
			Properties p = new Properties();
			int itemSize = getSize();
			p.put("itemSize", Integer.toString(itemSize));
			int i = 0;
			int id = 0;
			for (i = 0; i < itemSize; i++) {
				p.put("itemPriority" + i, Integer.toString(priority.get(i)));
				p.put("itemID" + i, Integer.toString(ID.get(i)));
				p.put("itemPrice" + i, Integer.toString(price.get(i)));
				p.put("itemNoted" + i, Boolean.toString(noted.get(i)));
				p.put("itemStackable", Boolean.toString(stackable.get(i)));
				p.put("itemName" + i, name.get(i));
			}
			try {
				p.store(new FileOutputStream(itemFile), "");
			} catch (Exception e) {
				log("index " + i + " id " + id);
				e.printStackTrace();
			}
			log("Items Saved back");
		}

		public void update(){
			Loot tempArray = new Loot();
			tempArray = this;
			this.load();
			for(int i = 0; i < tempArray.getSize(); i++){
				boolean add = true;
				for(int j = 0; j < this.getSize(); j++){
					if(tempArray.getID(i) == this.getID(j)){
						add = false;
						break;
					}
				}
				if(add){
					int id = tempArray.getID(i);
					this.add(tempArray.getPriority(id), tempArray.isNoted(id), tempArray.isStackable(id), id, tempArray.getPrice(id), tempArray.getName(id));
				}

				/*				int id = ID.get(i);
				for(int ids:tempArray.getPickedUpIDS()){
					if(ids == id){
						this.pickedUpIDS.add(id);
						this.pickedUpAmounts.add(tempArray.getPickedUpAmount(id));
					}
				}*/
			}
		}

		private class InitThread extends Thread{
			int id = 0;
			public void setID(int id){
				this.id = id;
			}
			public void start(){
				int index = ID.indexOf(id);
				if(id == COINS){
					price.set(index, 1);
					name.set(index, "Coins");
				}else{
					GEItem item;
					if(noted.get(index))
						item = grandExchange.lookup(id - 1);
					else
						item = grandExchange.lookup(id);
					if(item == null){
						item = grandExchange.lookup(id - 1);
						if(item != null && item.getName().toLowerCase() != name.get(index).toLowerCase())
							item = null;
						else{
							noted.set(index, true);
							stackable.set(index, true);
						}
					}
					if(item != null){
						price.set(index, item.getGuidePrice());
						name.set(index, item.getName());
						geItem.set(index, item);
					}else{
						price.set(index, 0);
						if(name.get(index).equals("") || 
								name.get(index).equals(Integer.toString(id))){
							for(int i = 0; i < noValue.length;i++){
								if(noValue[i] == id){
									name.set(index, noValueName[i]);
									break;
								}
							}
						}
					}
				}
			}
		}
	}

	private Image getImage(String url) {
		try {
			return ImageIO.read(new URL(url));
		} catch(IOException e) {
			return null;
		}
	}

	public void onRepaint(Graphics g1) {



		Timer runTime = new Timer((int)System.currentTimeMillis() - startTime);
		String convertedRunTime = runTime.toRemainingString();

		int profitPerHour = (int) ((3600000.0 / runTime.getRemaining()) * profit);

		String dispProfit = "" + profit;
		String dispProfitPerHour = "" + profitPerHour;
		if(profit < 0){
			dispProfit = "(" + (-profit) + ")";
			dispProfitPerHour = "(" + (-profitPerHour) + ")";
		}
		Graphics2D g = (Graphics2D)g1;
		g.setStroke(new BasicStroke(1));
		if(!showPaint){
			g.setFont(ARIAL);
			g.setColor(Color.white);
			g.drawString("Click Chat To See Paint", 160, 335);
		}else{
			g.setColor(Color.black);
			g.fillRect(8, 344, 505, 128);
			g.setFont(ARIAL);
			g.setColor(Color.green);
			g.drawString("konzy's Cockroach Killer V " + properties.version(), 10, 380);
			
			g.drawString("konzy's Cockroach Killer V " + properties.version(), 10, 380);
			
			g.drawString("By konzy", 10, 410);
			g.drawString("Status: " + status, 10, 430);
			g.drawImage(COCKROACH_IMAGE, 302, 271, null);
			
			if(outOfDate){
				g.setColor(Color.red);
				g.drawString("Please Update to " + currentVersion, 10, 395);
			}else{
				g.setColor(Color.green);
				g.drawString("Script is Current", 10, 395);
			}

		}
		g.setColor(Color.white);
		g.fillRoundRect(94, 339, 91, 22, 16, 16);
		g.fillRoundRect(3, 339, 91, 22, 16, 16);
		g.fillRoundRect(185, 339, 91, 22, 16, 16);
		g.setColor(Color.black);
		g.drawRoundRect(3, 339, 91, 22, 16, 16);
		g.drawRoundRect(94, 339, 91, 22, 16, 16);
		g.drawRoundRect(185, 339, 91, 22, 16, 16);
		g.setFont(largeFont);
		g.drawString("Info", 27, 356);
		g.drawString("XP", 131, 358);
		g.drawString("Loot", 210, 357);
		int spacer = smallFont.getSize() + 2;
		if(infoBool || tempInfo){
			g.setColor(Color.black);
			g.fillRoundRect(3, 242, 91, 88, 16, 16);
			g.setColor(Color.white);
			g.drawRoundRect(3, 242, 91, 88, 16, 16);
			g.setFont(smallFont);
			g.drawString(convertedRunTime, 18, 320);
			g.drawString("Run Time:", 8, 320 - spacer);
			g.drawString(dispProfit, 8, 320 - 2 * spacer);
			g.drawString("Profit:", 8, 320 - 3 * spacer);
			g.drawString(dispProfitPerHour, 8, 320 - 4 * spacer);
			g.drawString("Profit Per Hour:", 8, 320 - 5 * spacer);
		}
		if(XPBool || tempXP){
			ArrayList<Integer> gainedSkills = new ArrayList<Integer>(25);
			for(int i = 0; i < 24; i++){
				if(startXP[i] != skills.getCurrentExp(i)){
					gainedSkills.add(i);
				}
			}
			int j = gainedSkills.size();
			g.setColor(Color.black);
			g.fillRoundRect(94, 310 - spacer * j, 91, spacer * j + 20, 16, 16);
			g.setColor(Color.white);
			g.drawRoundRect(94, 310 - spacer * j, 91, spacer * j + 20, 16, 16);
			g.setFont(smallFont);
			if(gainedSkills != null){
				int i = 1;
				for(int s:gainedSkills){
					g.setColor(Color.white);
					g.fillRect(97, 320 - i * spacer, 84, 11);
					g.setColor(Color.red);
					g.drawRect(97, 320 - i * spacer, 84, 11);
					g.fillRect(97, 320 - i * spacer, 84 * skills.getPercentToNextLevel(s)/100, 11);
					g.setColor(Color.black);
					g.drawString("" + Skills.SKILL_NAMES[s] + " lvl " + skills.getCurrentLevel(s)
							, 99, 330 - spacer * i++);
				}
			}
		}

		if(lootBool || tempLoot){
			int size = lootArray.pickedUpSize();
			int ids[] = lootArray.getPickedUpIDS();

			g.setColor(Color.black);
			g.fillRoundRect(185, 310 - size * spacer, lootLength, size * spacer + 20, 16, 16);
			g.setColor(Color.white);
			g.drawRoundRect(185, 310 - size * spacer, lootLength, size * spacer + 20, 16, 16);
			g.setFont(smallFont);
			for(int i = 0; i < size; i++){
				String s = lootArray.getName(ids[i]) + " x " + lootArray.getPickedUpAmount(ids[i]);

				if(s.length() * 6 > lootLength )
					lootLength = s.length() * 6;
				g.drawString(s, 190, 330 - spacer * (i + 1));
			}
		}

		Point x = mouse.getLocation();
		Point y = mouse.getPressLocation();

		g.setColor(Color.green);

		g.drawOval(x.x - 2, x.y - 2, 5, 5);
		g.drawOval(x.x - 5, x.y - 5, 11, 11);

		if ((System.currentTimeMillis() - mouse.getPressTime()) < 3000) {
			g.setColor(Color.red);
			g.drawOval(y.x - 2, y.y - 2, 4, 4);
			g.drawOval(y.x - 5, y.y - 5, 10, 10);
		}
	}

	//END: Paint generated using Enfilade's Easel

	@SuppressWarnings("serial")
	public class konzyCockroachKillerGUI extends javax.swing.JFrame {

		private boolean moveItemLeft = true;
		private final File saveFile = new File(getCacheDirectory()
				+ System.getProperty("file.separator")
				+ String.valueOf(Math.abs(account.getName().hashCode())) + ".ini");
		ArrayList<String> highArray = new ArrayList<String>(50);
		ArrayList<String> mediumArray = new ArrayList<String>(50);
		ArrayList<String> ignoreArray = new ArrayList<String>(50);

		/** Creates new form NewJFrame */
		public konzyCockroachKillerGUI() {
			initComponents();
			this.setLocationRelativeTo(null);
			setVisible(true);
		}

		/** This method is called from within the constructor to
		 * initialize the form.
		 * WARNING: Do NOT modify this code. The content of this method is
		 * always regenerated by the Form Editor.
		 */
		// <editor-fold defaultstate="collapsed" desc="Generated Code">
		private void initComponents() {

			Properties props = loadProperties();
			for (int i = 0; i < lootArray.getSize(); i++) {
				int id = lootArray.getID(i);
				if(lootArray.getPriority(id) == 1)
					highArray.add(lootArray.getName(id) + " " + lootArray.getPrice(id));
				else if(lootArray.getPriority(id) == 2)
					mediumArray.add(lootArray.getName(id) + " " + lootArray.getPrice(id));
				else
					ignoreArray.add(lootArray.getName(id) + " " + lootArray.getPrice(id));
			}
			titleLabel = new javax.swing.JLabel();
			screenOnExitLabel = new javax.swing.JLabel();
			worldJumpTimeHrLabel = new javax.swing.JLabel();
			floorSwitchTimeMinLabel = new javax.swing.JLabel();
			worldJumpTimeHrTextBox = new javax.swing.JTextField();
			floorSwitchTimeMinTextBox = new javax.swing.JTextField();
			screenOnExitCheckBox = new javax.swing.JCheckBox();
			mouseSpeedLabel = new javax.swing.JLabel();
			mouseSpeedSlider = new javax.swing.JSlider();
			startButton = new javax.swing.JButton();
			foodSelectLabel = new javax.swing.JLabel();
			customFoodIDLabel = new javax.swing.JLabel();
			customFoodIDTextBox = new javax.swing.JTextField();
			foodSelectComboBox = new javax.swing.JComboBox();
			settingsTabbedPane = new javax.swing.JTabbedPane();
			levelsAndPotionsPanel = new javax.swing.JPanel();
			strengthLevelLabel = new javax.swing.JLabel();
			attackLevelLabel = new javax.swing.JLabel();
			attackLevelTextBox = new javax.swing.JTextField();
			defenceLevelLabel = new javax.swing.JLabel();
			strengthLevelTextBox = new javax.swing.JTextField();
			defenceLevelTextBox = new javax.swing.JTextField();
			strengthPotionLabel = new javax.swing.JLabel();
			attackPotionLabel = new javax.swing.JLabel();
			combatPotionTextBox = new javax.swing.JTextField();
			attackPotionTextBox = new javax.swing.JTextField();
			combatPotionLabel = new javax.swing.JLabel();
			defencePotionTextBox = new javax.swing.JTextField();
			defencePotionLabel = new javax.swing.JLabel();
			strengthPotionTextBox = new javax.swing.JTextField();
			combatLevelLabel = new javax.swing.JLabel();
			potionLabel = new javax.swing.JLabel();
			lootSettingsPanel = new javax.swing.JPanel();
			moveRightButton = new javax.swing.JButton();
			itemMovementLabel = new javax.swing.JLabel();
			moveLeftButton = new javax.swing.JButton();
			ignorePriorityLabel = new javax.swing.JLabel();
			mediumPriorityLabel = new javax.swing.JLabel();
			mediumSortScrollPane = new javax.swing.JScrollPane();
			mediumList = new javax.swing.JList();
			highPriorityLabel = new javax.swing.JLabel();
			ignoreSortScrollPane = new javax.swing.JScrollPane();
			ignoreList = new javax.swing.JList();
			highSortScrollPane = new javax.swing.JScrollPane();
			highList = new javax.swing.JList();
			enableSpecialLabel = new javax.swing.JLabel();
			enableSpecialCheckBox = new javax.swing.JCheckBox();

			setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
			setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
			setMinimumSize(new java.awt.Dimension(506, 643));
			setResizable(false);

			titleLabel.setFont(new java.awt.Font("Tahoma", 0, 18));
			titleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
			titleLabel.setText("konzy's Cockroach Soldier Killer");

			screenOnExitLabel.setFont(new java.awt.Font("Tahoma", 0, 12));
			screenOnExitLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
			screenOnExitLabel.setText("Screenshot on exit");

			worldJumpTimeHrLabel.setFont(new java.awt.Font("Tahoma", 0, 12));
			worldJumpTimeHrLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
			worldJumpTimeHrLabel.setText("Time in between world jumps (hr)");

			floorSwitchTimeMinLabel.setFont(new java.awt.Font("Tahoma", 0, 12));
			floorSwitchTimeMinLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
			floorSwitchTimeMinLabel.setText("Time in between floor switches (min)");

			worldJumpTimeHrTextBox.setText("2");
			worldJumpTimeHrTextBox.setToolTipText("Sets the time between jumping to a random world with 30min randomness");

			floorSwitchTimeMinTextBox.setText("30");
			floorSwitchTimeMinTextBox.setToolTipText("Sets the time between switching from one floor to the other with 15min randomness");

			screenOnExitCheckBox.setToolTipText("Takes a screenshot and saves it in your RSbot\\Screenshots\\ folder when the bot shuts down so you can post a screenie");

			mouseSpeedLabel.setFont(new java.awt.Font("Tahoma", 0, 12));
			mouseSpeedLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
			mouseSpeedLabel.setText("How Fast to move mouse (slow to fast)");

			mouseSpeedSlider.setMaximum(10);
			mouseSpeedSlider.setMinimum(1);
			mouseSpeedSlider.setMinorTickSpacing(1);
			mouseSpeedSlider.setPaintTicks(true);
			mouseSpeedSlider.setSnapToTicks(true);
			mouseSpeedSlider.setToolTipText("How fast you want the mouse to move, from very slow to extremely fast");

			startButton.setBackground(java.awt.Color.red);
			startButton.setText("Start");
			startButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					startButtonActionPerformed(evt);
				}
			});

			foodSelectLabel.setFont(new java.awt.Font("Tahoma", 0, 12));
			foodSelectLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
			foodSelectLabel.setText("Food");

			customFoodIDLabel.setFont(new java.awt.Font("Tahoma", 0, 12));
			customFoodIDLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
			customFoodIDLabel.setText("Custom Food ID");

			customFoodIDTextBox.setText("379");
			customFoodIDTextBox.setToolTipText("Type in the ID of the food you want to use here");

			foodSelectComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Custom", "Herring", "Mackerel", "Trout", "Cod", "Pike", "Salmon", "Tuna", "Cake", "Lobster", "Bass", "Swordfish", "Potato with cheese", "Monkfish", "Shark", "Sea turtle", "Manta ray", "Tuna potato", "Rocktail" }));
			foodSelectComboBox.setSelectedIndex(9);
			foodSelectComboBox.setToolTipText("Choose the food you want to use from the dropdown menu, if you can't find the food on the list then chose custom and enter the ID for the food below");

			strengthLevelLabel.setFont(new java.awt.Font("Tahoma", 0, 12));
			strengthLevelLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
			strengthLevelLabel.setText("Strength");

			attackLevelLabel.setFont(new java.awt.Font("Tahoma", 0, 12));
			attackLevelLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
			attackLevelLabel.setText("Attack");

			attackLevelTextBox.setText("99");
			attackLevelTextBox.setToolTipText("");

			defenceLevelLabel.setFont(new java.awt.Font("Tahoma", 0, 12));
			defenceLevelLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
			defenceLevelLabel.setText("Defence");

			strengthLevelTextBox.setText("99");
			strengthLevelTextBox.setToolTipText("");

			defenceLevelTextBox.setText("100");
			defenceLevelTextBox.setToolTipText("");

			strengthPotionLabel.setFont(new java.awt.Font("Tahoma", 0, 12));
			strengthPotionLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
			strengthPotionLabel.setText("Strength");

			attackPotionLabel.setFont(new java.awt.Font("Tahoma", 0, 12));
			attackPotionLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
			attackPotionLabel.setText("Attack");

			combatPotionTextBox.setText("0");
			combatPotionTextBox.setToolTipText("");

			attackPotionTextBox.setText("0");
			attackPotionTextBox.setToolTipText("");

			combatPotionLabel.setFont(new java.awt.Font("Tahoma", 0, 12));
			combatPotionLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
			combatPotionLabel.setText("Combat");

			defencePotionTextBox.setText("0");
			defencePotionTextBox.setToolTipText("");

			defencePotionLabel.setFont(new java.awt.Font("Tahoma", 0, 12));
			defencePotionLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
			defencePotionLabel.setText("Defence");

			strengthPotionTextBox.setText("0");
			strengthPotionTextBox.setToolTipText("");

			combatLevelLabel.setText("Combat levels to train to");

			potionLabel.setText("Potions to get when banking");

			javax.swing.GroupLayout levelsAndPotionsPanelLayout = new javax.swing.GroupLayout(levelsAndPotionsPanel);
			levelsAndPotionsPanel.setLayout(levelsAndPotionsPanelLayout);
			levelsAndPotionsPanelLayout.setHorizontalGroup(
					levelsAndPotionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(levelsAndPotionsPanelLayout.createSequentialGroup()
							.addContainerGap()
							.addGroup(levelsAndPotionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
									.addGroup(levelsAndPotionsPanelLayout.createSequentialGroup()
											.addComponent(defenceLevelLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
											.addGap(18, 18, 18)
											.addComponent(defenceLevelTextBox, javax.swing.GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE))
											.addGroup(levelsAndPotionsPanelLayout.createSequentialGroup()
													.addComponent(strengthLevelLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
													.addGap(18, 18, 18)
													.addComponent(strengthLevelTextBox, javax.swing.GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE))
													.addGroup(levelsAndPotionsPanelLayout.createSequentialGroup()
															.addComponent(attackLevelLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
															.addGap(18, 18, 18)
															.addComponent(attackLevelTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
															.addComponent(combatLevelLabel, javax.swing.GroupLayout.Alignment.TRAILING))
															.addGap(47, 47, 47)
															.addGroup(levelsAndPotionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
																	.addGroup(levelsAndPotionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
																			.addGroup(levelsAndPotionsPanelLayout.createSequentialGroup()
																					.addComponent(combatPotionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
																					.addGap(18, 18, 18)
																					.addComponent(combatPotionTextBox))
																					.addGroup(levelsAndPotionsPanelLayout.createSequentialGroup()
																							.addComponent(defencePotionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
																							.addGap(18, 18, 18)
																							.addComponent(defencePotionTextBox))
																							.addGroup(levelsAndPotionsPanelLayout.createSequentialGroup()
																									.addComponent(strengthPotionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
																									.addGap(18, 18, 18)
																									.addComponent(strengthPotionTextBox))
																									.addGroup(levelsAndPotionsPanelLayout.createSequentialGroup()
																											.addComponent(attackPotionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
																											.addGap(18, 18, 18)
																											.addComponent(attackPotionTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)))
																											.addComponent(potionLabel))
																											.addContainerGap(110, Short.MAX_VALUE))
			);
			levelsAndPotionsPanelLayout.setVerticalGroup(
					levelsAndPotionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(levelsAndPotionsPanelLayout.createSequentialGroup()
							.addGap(33, 33, 33)
							.addGroup(levelsAndPotionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
									.addComponent(combatLevelLabel)
									.addComponent(potionLabel))
									.addGap(18, 18, 18)
									.addGroup(levelsAndPotionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
											.addGroup(levelsAndPotionsPanelLayout.createSequentialGroup()
													.addGroup(levelsAndPotionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
															.addComponent(attackPotionLabel)
															.addComponent(attackPotionTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
															.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
															.addGroup(levelsAndPotionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																	.addComponent(strengthPotionLabel)
																	.addComponent(strengthPotionTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
																	.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																	.addGroup(levelsAndPotionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																			.addComponent(defencePotionLabel)
																			.addComponent(defencePotionTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
																			.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																			.addGroup(levelsAndPotionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																					.addComponent(combatPotionLabel)
																					.addComponent(combatPotionTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
																					.addGroup(levelsAndPotionsPanelLayout.createSequentialGroup()
																							.addGroup(levelsAndPotionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																									.addComponent(attackLevelLabel)
																									.addComponent(attackLevelTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
																									.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																									.addGroup(levelsAndPotionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																											.addComponent(strengthLevelLabel)
																											.addComponent(strengthLevelTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
																											.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																											.addGroup(levelsAndPotionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																													.addComponent(defenceLevelLabel)
																													.addComponent(defenceLevelTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
																													.addContainerGap(60, Short.MAX_VALUE))
			);

			if(!memberAccount)
				attackPotionLabel.setVisible(false);
			if(!memberAccount)
				combatPotionTextBox.setVisible(false);
			if(props.getProperty("combatPotionsToGet") != null)
				combatPotionTextBox.setText(props.getProperty("combatPotionsToGet"));
			if(!memberAccount)
				attackPotionTextBox.setVisible(false);
			if(props.getProperty("attackPotionsToGet") != null)
				attackPotionTextBox.setText(props.getProperty("attackPotionsToGet"));
			if(!memberAccount)
				combatPotionLabel.setVisible(false);
			if(!memberAccount)
				defencePotionTextBox.setVisible(false);
			if(props.getProperty("defencePotionsToGet") != null)
				defencePotionTextBox.setText(props.getProperty("defencePotionsToGet"));
			if(!memberAccount)
				defencePotionLabel.setVisible(false);
			if(props.getProperty("strengthPotionsToGet") != null)
				strengthPotionTextBox.setText(props.getProperty("strengthPotionsToGet"));

			settingsTabbedPane.addTab("Combat Levels and Potions", levelsAndPotionsPanel);

			moveRightButton.setText(">>");
			moveRightButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					moveRightButtonActionPerformed(evt);
				}
			});

			itemMovementLabel.setText("Moves Higher Priority");

			moveLeftButton.setText("<<");
			moveLeftButton.setEnabled(false);
			moveLeftButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					moveLeftButtonActionPerformed(evt);
				}
			});

			ignorePriorityLabel.setText("Ignore");

			mediumPriorityLabel.setText("Medium Priority");

			mediumList.setModel(new javax.swing.AbstractListModel() {
				public int getSize() { return mediumArray.size(); }
				public Object getElementAt(int i) { return mediumArray.get(i); }
			});
			mediumList.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent evt) {
					mediumListMouseClicked(evt);
				}
			});
			mediumSortScrollPane.setViewportView(mediumList);

			highPriorityLabel.setText("High Priority");

			ignoreList.setModel(new javax.swing.AbstractListModel() {
				public int getSize() { return ignoreArray.size(); }
				public Object getElementAt(int i) { return ignoreArray.get(i); }
			});
			ignoreList.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent evt) {
					ignoreListMouseClicked(evt);
				}
			});
			ignoreSortScrollPane.setViewportView(ignoreList);

			highList.setModel(new javax.swing.AbstractListModel() {
				public int getSize() { return highArray.size(); }
				public Object getElementAt(int i) { return highArray.get(i); }
			});
			highList.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent evt) {
					highListMouseClicked(evt);
				}
			});
			highSortScrollPane.setViewportView(highList);

			javax.swing.GroupLayout lootSettingsPanelLayout = new javax.swing.GroupLayout(lootSettingsPanel);
			lootSettingsPanel.setLayout(lootSettingsPanelLayout);
			lootSettingsPanelLayout.setHorizontalGroup(
					lootSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(lootSettingsPanelLayout.createSequentialGroup()
							.addGap(18, 18, 18)
							.addGroup(lootSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
									.addComponent(highSortScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
									.addGroup(lootSettingsPanelLayout.createSequentialGroup()
											.addGap(43, 43, 43)
											.addComponent(highPriorityLabel)))
											.addGroup(lootSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
													.addGroup(lootSettingsPanelLayout.createSequentialGroup()
															.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
															.addGroup(lootSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
																	.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, lootSettingsPanelLayout.createSequentialGroup()
																			.addComponent(moveLeftButton)
																			.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
																			.addComponent(moveRightButton))
																			.addComponent(itemMovementLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
																			.addComponent(mediumSortScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)))
																			.addGroup(lootSettingsPanelLayout.createSequentialGroup()
																					.addGap(44, 44, 44)
																					.addComponent(mediumPriorityLabel)))
																					.addGroup(lootSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
																							.addGroup(lootSettingsPanelLayout.createSequentialGroup()
																									.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																									.addComponent(ignoreSortScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
																									.addGroup(lootSettingsPanelLayout.createSequentialGroup()
																											.addGap(69, 69, 69)
																											.addComponent(ignorePriorityLabel)))
																											.addGap(22, 22, 22))
			);
			lootSettingsPanelLayout.setVerticalGroup(
					lootSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(lootSettingsPanelLayout.createSequentialGroup()
							.addContainerGap()
							.addGroup(lootSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
									.addGroup(lootSettingsPanelLayout.createSequentialGroup()
											.addComponent(highPriorityLabel)
											.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
											.addComponent(highSortScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
											.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, lootSettingsPanelLayout.createSequentialGroup()
													.addGroup(lootSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
															.addComponent(ignoreSortScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
															.addGroup(lootSettingsPanelLayout.createSequentialGroup()
																	.addGroup(lootSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																			.addComponent(mediumPriorityLabel)
																			.addComponent(ignorePriorityLabel))
																			.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																			.addComponent(mediumSortScrollPane)))
																			.addGap(11, 11, 11)
																			.addGroup(lootSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
																					.addGroup(lootSettingsPanelLayout.createSequentialGroup()
																							.addGap(31, 31, 31)
																							.addGroup(lootSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																									.addComponent(moveLeftButton)
																									.addComponent(moveRightButton)))
																									.addComponent(itemMovementLabel))))
																									.addContainerGap(12, Short.MAX_VALUE))
			);

			settingsTabbedPane.addTab("Loot Settings", lootSettingsPanel);

			enableSpecialLabel.setFont(new java.awt.Font("Tahoma", 0, 12));
			enableSpecialLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
			enableSpecialLabel.setText("Enable Special");
			if(!memberAccount)
				enableSpecialLabel.setVisible(false);

			enableSpecialCheckBox.setToolTipText("");

			javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
			getContentPane().setLayout(layout);
			layout.setHorizontalGroup(
					layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
							.addContainerGap()
							.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
									.addComponent(startButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
									.addComponent(settingsTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
									.addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
											.addGap(22, 22, 22)
											.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
													.addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
															.addGap(115, 115, 115)
															.addComponent(screenOnExitLabel)
															.addGap(18, 18, 18)
															.addComponent(screenOnExitCheckBox))
															.addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
																	.addGap(142, 142, 142)
																	.addComponent(enableSpecialLabel)
																	.addGap(18, 18, 18)
																	.addComponent(enableSpecialCheckBox))
																	.addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
																			.addGap(30, 30, 30)
																			.addComponent(worldJumpTimeHrLabel))
																			.addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
																					.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
																							.addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
																									.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
																											.addComponent(mouseSpeedLabel)
																											.addComponent(floorSwitchTimeMinLabel))
																											.addGap(18, 18, 18)
																											.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
																													.addComponent(worldJumpTimeHrTextBox, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
																													.addComponent(floorSwitchTimeMinTextBox, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
																													.addComponent(mouseSpeedSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)))
																													.addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
																															.addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
																																	.addGap(131, 131, 131)
																																	.addComponent(customFoodIDLabel)
																																	.addGap(18, 18, 18)
																																	.addComponent(customFoodIDTextBox))
																																	.addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
																																			.addGap(192, 192, 192)
																																			.addComponent(foodSelectLabel)
																																			.addGap(18, 18, 18)
																																			.addComponent(foodSelectComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
																																			.addGap(39, 39, 39)))
																																			.addGap(79, 79, 79))))
																																			.addComponent(titleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(
					layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(layout.createSequentialGroup()
							.addContainerGap()
							.addComponent(titleLabel)
							.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
							.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
									.addComponent(screenOnExitLabel)
									.addComponent(screenOnExitCheckBox))
									.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
									.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
											.addComponent(enableSpecialLabel)
											.addComponent(enableSpecialCheckBox))
											.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
											.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
													.addComponent(foodSelectLabel)
													.addComponent(foodSelectComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
													.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
													.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
															.addComponent(customFoodIDLabel)
															.addComponent(customFoodIDTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
															.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
															.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																	.addComponent(worldJumpTimeHrLabel)
																	.addComponent(worldJumpTimeHrTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
																	.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																	.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																			.addComponent(floorSwitchTimeMinLabel)
																			.addComponent(floorSwitchTimeMinTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
																			.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																			.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
																					.addComponent(mouseSpeedLabel)
																					.addComponent(mouseSpeedSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
																					.addGap(30, 30, 30)
																					.addComponent(settingsTabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE)
																					.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																					.addComponent(startButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																					.addContainerGap())
			);

			if(props.getProperty("worldJumpTimeHr") != null)
				worldJumpTimeHrTextBox.setText(props.getProperty("worldJumpTimeHr"));
			if(props.getProperty("floorSwitchTimeMin") != null)
				floorSwitchTimeMinTextBox.setText(props.getProperty("floorSwitchTimeMin"));
			if(props.getProperty("screenOnExit") != null &&
					props.getProperty("screenOnExit").equals("true"))
				screenOnExitCheckBox.setSelected(true);
			if(props.getProperty("mouseSpeed") != null)
				mouseSpeedSlider.setValue(Integer.parseInt(props.getProperty("mouseSpeed")));
			if(props.getProperty("customFoodID") != null)
				customFoodIDTextBox.setText(props.getProperty("customFoodID"));
			if(props.getProperty("foodSelect") != null)
				foodSelectComboBox.setSelectedItem(props.getProperty("foodSelect"));
			if(props.getProperty("enableSpecial") != null &&
					props.getProperty("enableSpecial").equals("true"))
				enableSpecialCheckBox.setSelected(true);
			if(!memberAccount)
				enableSpecialCheckBox.setVisible(false);

			pack();
		}// </editor-fold>

		private void listInit() {
			mediumList.setModel(new javax.swing.AbstractListModel() {

				public int getSize() {
					return mediumArray.size();
				}

				public Object getElementAt(int i) {
					return mediumArray.get(i);
				}
			});
			mediumSortScrollPane.setViewportView(mediumList);

			ignoreList.setModel(new javax.swing.AbstractListModel() {

				public int getSize() {
					return ignoreArray.size();
				}

				public Object getElementAt(int i) {
					return ignoreArray.get(i);
				}
			});
			ignoreSortScrollPane.setViewportView(ignoreList);

			highList.setModel(new javax.swing.AbstractListModel() {

				public int getSize() {
					return highArray.size();
				}

				public Object getElementAt(int i) {
					return highArray.get(i);
				}
			});
			highSortScrollPane.setViewportView(highList);
		}

		private void mediumListMouseClicked(java.awt.event.MouseEvent evt) {                                        
			String movingItem = mediumArray.get(mediumList.getSelectedIndex());
			mediumArray.remove(mediumList.getSelectedIndex());
			if (moveItemLeft) {
				highArray.add(movingItem);
			} else {
				ignoreArray.add(movingItem);
			}
			listInit();
		}                                       

		private void highListMouseClicked(java.awt.event.MouseEvent evt) {
			String movingItem = highArray.get(highList.getSelectedIndex());
			if (!moveItemLeft) {
				mediumArray.add(movingItem);
				highArray.remove(highList.getSelectedIndex());
			}
			listInit();
		}

		private void ignoreListMouseClicked(java.awt.event.MouseEvent evt) {
			String movingItem = ignoreArray.get(ignoreList.getSelectedIndex());
			if (moveItemLeft) {
				mediumArray.add(movingItem);
				ignoreArray.remove(ignoreList.getSelectedIndex());
			}
			listInit();
		}

		private void moveLeftButtonActionPerformed(java.awt.event.ActionEvent evt) {
			itemMovementLabel.setText("Moves Higher Priority");
			moveLeftButton.setEnabled(false);
			moveRightButton.setEnabled(true);
			moveItemLeft = true;
		}

		private void moveRightButtonActionPerformed(java.awt.event.ActionEvent evt) {
			itemMovementLabel.setText("Moves Lower Priority");
			moveLeftButton.setEnabled(true);
			moveRightButton.setEnabled(false);
			moveItemLeft = false;
		}

		private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {

			for(String s:highArray){
				for(int i = 0; i < lootArray.getSize(); i++){
					int id = lootArray.getID(i);
					if(s.contains(lootArray.getName(id))){
						lootArray.setPriority(id, 1);
					}
				}
			}
			for(String s:mediumArray){
				for(int i = 0; i < lootArray.getSize(); i++){
					int id = lootArray.getID(i);
					if(s.contains(lootArray.getName(id))){
						lootArray.setPriority(id, 2);
					}
				}
			}
			for(String s:ignoreArray){
				for(int i = 0; i < lootArray.getSize(); i++){
					int id = lootArray.getID(i);
					if(s.contains(lootArray.getName(id))){
						lootArray.setPriority(id, 3);
					}
				}
			}
			lootArray.save();
			saveProperties();
			screenshotOnExit = screenOnExitCheckBox.isSelected();

			enableSpecial = enableSpecialCheckBox.isSelected();
			mouseDelay = mouseSpeedSlider.getMaximum() - mouseSpeedSlider.getValue();
			try {
				attackPotionsToGet = Integer.parseInt(attackPotionTextBox.getText());
				strengthPotionsToGet = Integer.parseInt(strengthPotionTextBox.getText());
				defencePotionsToGet = Integer.parseInt(defencePotionTextBox.getText());
				combatPotionsToGet = Integer.parseInt(combatPotionTextBox.getText());
				attackLevelStop = Integer.parseInt(attackLevelTextBox.getText());
				strengthLevelStop = Integer.parseInt(strengthLevelTextBox.getText());
				defenceLevelStop = Integer.parseInt(defenceLevelTextBox.getText());
				foodID = Integer.parseInt(customFoodIDTextBox.getText());
				String s = foodSelectComboBox.getSelectedItem().toString();
				for (int i = 0; i < SELECTABLE_FOOD.length; i++) {
					if (s == SELECTABLE_FOOD[i]) {
						foodID = SELECTABLE_FOOD_IDS[i];
					}
				}
				worldJumpTimeMs = Integer.parseInt(worldJumpTimeHrTextBox.getText()) * 1000 * 60 * 60;
				floorSwitchTimeMs = Integer.parseInt(floorSwitchTimeMinTextBox.getText()) * 1000 * 60;
				worldSwitchTimer = new Timer(worldJumpTimeMs);
				floorSwitchTimer = new Timer(floorSwitchTimeMs);
			} catch (NumberFormatException e1) {
			}
			startScript = true;
			dispose();
		}



		private Properties loadProperties() {
			try {
				Properties p = new Properties();
				if(!saveFile.exists()){
					saveFile.createNewFile();
				}
				p.load(new FileInputStream(saveFile));
				return p;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		private void saveProperties() {
			Properties p = new Properties();
			p.put("attackPotionsToGet", attackPotionTextBox.getText());
			p.put("strengthPotionsToGet", strengthPotionTextBox.getText());
			p.put("defencePotionsToGet", defencePotionTextBox.getText());
			p.put("combatPotionsToGet", combatPotionTextBox.getText());
			p.put("attackLevelStop", attackLevelTextBox.getText());
			p.put("strengthLevelStop", strengthLevelTextBox.getText());
			p.put("defenceLevelStop", defenceLevelTextBox.getText());
			p.put("enableSpecial", Boolean.toString(enableSpecialCheckBox.isSelected()));
			p.put("screenOnExit", Boolean.toString(screenOnExitCheckBox.isSelected()));
			p.put("customFoodID", customFoodIDTextBox.getText());
			p.put("worldJumpTimeHr", worldJumpTimeHrTextBox.getText());
			p.put("floorSwitchTimeMin", floorSwitchTimeMinTextBox.getText());
			p.put("mouseSpeed", Integer.toString(mouseSpeedSlider.getValue()));
			p.put("foodSelect", foodSelectComboBox.getSelectedItem().toString());

			try {
				p.store(new FileOutputStream(saveFile), "");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Variables declaration - do not modify
		private javax.swing.JLabel attackLevelLabel;
		private javax.swing.JTextField attackLevelTextBox;
		private javax.swing.JLabel attackPotionLabel;
		private javax.swing.JTextField attackPotionTextBox;
		private javax.swing.JLabel combatLevelLabel;
		private javax.swing.JLabel combatPotionLabel;
		private javax.swing.JTextField combatPotionTextBox;
		private javax.swing.JLabel customFoodIDLabel;
		private javax.swing.JTextField customFoodIDTextBox;
		private javax.swing.JLabel defenceLevelLabel;
		private javax.swing.JTextField defenceLevelTextBox;
		private javax.swing.JLabel defencePotionLabel;
		private javax.swing.JTextField defencePotionTextBox;
		private javax.swing.JCheckBox enableSpecialCheckBox;
		private javax.swing.JLabel enableSpecialLabel;
		private javax.swing.JLabel floorSwitchTimeMinLabel;
		private javax.swing.JTextField floorSwitchTimeMinTextBox;
		private javax.swing.JComboBox foodSelectComboBox;
		private javax.swing.JLabel foodSelectLabel;
		private javax.swing.JList highList;
		private javax.swing.JLabel highPriorityLabel;
		private javax.swing.JScrollPane highSortScrollPane;
		private javax.swing.JList ignoreList;
		private javax.swing.JLabel ignorePriorityLabel;
		private javax.swing.JScrollPane ignoreSortScrollPane;
		private javax.swing.JLabel itemMovementLabel;
		private javax.swing.JPanel levelsAndPotionsPanel;
		private javax.swing.JPanel lootSettingsPanel;
		private javax.swing.JList mediumList;
		private javax.swing.JLabel mediumPriorityLabel;
		private javax.swing.JScrollPane mediumSortScrollPane;
		private javax.swing.JLabel mouseSpeedLabel;
		private javax.swing.JSlider mouseSpeedSlider;
		private javax.swing.JButton moveLeftButton;
		private javax.swing.JButton moveRightButton;
		private javax.swing.JLabel potionLabel;
		private javax.swing.JCheckBox screenOnExitCheckBox;
		private javax.swing.JLabel screenOnExitLabel;
		private javax.swing.JTabbedPane settingsTabbedPane;
		private javax.swing.JButton startButton;
		private javax.swing.JLabel strengthLevelLabel;
		private javax.swing.JTextField strengthLevelTextBox;
		private javax.swing.JLabel strengthPotionLabel;
		private javax.swing.JTextField strengthPotionTextBox;
		private javax.swing.JLabel titleLabel;
		private javax.swing.JLabel worldJumpTimeHrLabel;
		private javax.swing.JTextField worldJumpTimeHrTextBox;
		// End of variables declaration
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Point mLoc = e.getPoint();
		int x = (int)mLoc.getX();
		int y = (int)mLoc.getY();
		if(y > 339 && y < 361){
			if(x > 3 && x < 94){
				infoBool = !infoBool;
			}else if(x > 94 && x < 185){
				XPBool = !XPBool;
			}else if(x > 185 && x < 276){
				lootBool = !lootBool;
			}else if(x > 276 && x < 512){
				showPaint = !showPaint;
			}
		}else if(y > 361 && y < 479){
			if(x < 512)
				showPaint = !showPaint;
		}
	}
	@Override
	public void mouseMoved(MouseEvent e) {
		Point mLoc = e.getPoint();
		int x = (int)mLoc.getX();
		int y = (int)mLoc.getY();
		if(y > 339 && y < 361){
			if(x > 3 && x < 94){
				tempLoot = false;
				tempXP = false;
				tempInfo = true;
			}else if(x > 94 && x < 185){
				tempLoot = false;
				tempXP = true;
				tempInfo = false;
			}else if(x > 185 && x < 276){
				tempLoot = true;
				tempXP = false;
				tempInfo = false;
			}else{
				tempLoot = false;
				tempXP = false;
				tempInfo = false;
			}
		}else{
			tempLoot = false;
			tempXP = false;
			tempInfo = false;
		}

	}



	@Override
	public void mousePressed(MouseEvent e) { }

	@Override
	public void mouseReleased(MouseEvent e) { }

	@Override
	public void mouseEntered(MouseEvent e) { }

	@Override
	public void mouseExited(MouseEvent e) { }

	@Override
	public void messageReceived(MessageEvent arg0) { }

	@Override
	public void mouseDragged(MouseEvent arg0) {	}

		}