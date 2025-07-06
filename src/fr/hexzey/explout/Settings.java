package fr.hexzey.explout;

public class Settings
{
	// PARAMETRES GENERAUX
	public boolean allow_classes; // definir si l'usage de classes (kits avec des objets bonus) est autorise
	public int roundsToWin; // nombre de rounds a gagner pour qu'un joueur remporte la partie
	
	// PARAMETRES CLASSE SLIME
	public long slime_platform_life_ticks; // duree de vie de la plateforme en TICKS (avant sa disparition)
	public int slime_platform_range; // rayon de la plateforme (bloc central exclu)
	public float slime_platform_velocity_Y; // intensite de la velocite du joueur sur l'axe Y lorsqu'il touche un slime block
	
	// PARAMETRES CLASSE GOLEM
	public long golem_armor_duration; // duree de vie de l'armure en TICKS
	public float golem_kb_reduction_multiplier; // multiplicateur de kb de l'armure
	public float golem_kb_tnt_reduction_multiplier;
	
	// PARAMETRES CLASSE CACTUS
	public long cactus_thorns_duration; // duree pendant laquelle les kb subis sont rendus aux adversaires
	public float cactus_thorns_kb_multiplier; // intensite du kb renvoye (1 = meme kb que celui recu)
	
	public int snowballStack; // nombre de snowballs max
	public long snowballReloadTime; // temps de rechargement des snowballs en TICKS
	
	public int tnt_fuse_ticks; // nombre de TICKS avant l'explosion d'une tnt placee par un joueur
	
	public int maxHooks; // nombre d'utilisations max de la canne a peche
	
	public float kb_multiplicator_incrementation; // augmentation du kb
	public int kb_multiplicator_seconds; // temps entre chaque augmentation du kb en SECONDES
	
	public float explosion_kb_default; // intensite du kb applique par une explosion (ex: tnt)
	public float close_combat_kb_default; // intensite de kb applique lors d'un duel au corps-a-corps
	
	
	public Settings()
	{
		// PARAMETRES GENERAUX
		allow_classes = true;
		roundsToWin = 3;
		
		// PARAMETRES CLASSE SLIME
		slime_platform_life_ticks = 3*20L;
		slime_platform_velocity_Y = 6.8f;
		slime_platform_range = 1;
		
		// PARAMETRES CLASSE GOLEM
		golem_armor_duration = 9*20L;
		golem_kb_reduction_multiplier = 0.1f;
		golem_kb_tnt_reduction_multiplier = 0.5f;
		
		// PARAMETRES CLASSE CACTUS
		cactus_thorns_duration = 9*20L;
		cactus_thorns_kb_multiplier = 2.75f;
		
		// PARAMETRES SNOWBALLS
		snowballStack = 5;
		snowballReloadTime = 4*20L;
		
		// PARAMETRES TNT
		tnt_fuse_ticks = 10;
		explosion_kb_default = 1.5f;
		
		// PARAMETRES CANNE A PECHE
		maxHooks = 5;
		
		// PARAMETRES MULTIPLICATEUR DE KB
		kb_multiplicator_incrementation = 0.15f;
		kb_multiplicator_seconds = 6;
		
		close_combat_kb_default = 2.5f;
	}
}
