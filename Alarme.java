// Copyright (C) 2017 Alexandre-Xavier Labonté-Lamoureux

// Ce programme n'est pas programmé très rigoureusement.
// Je ne suis pas responsable des gens qui ne se réveillent pas.

// Interface graphique
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.GridLayout;

// Fichiers
import java.io.File;

// Numeric boxes
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

// Date / Time
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Sound
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import javafx.embed.swing.JFXPanel;

// Classe d'un thread qui roule dans le fond
class Veille implements Runnable {

	int tics = 0;
	final int THRES = 3;

	public Veille(SpinnerModel h, SpinnerModel m) {
		// Doit être initialisé pour que le son fonctionne
		final JFXPanel fxPanel = new JFXPanel();

		// Beurk!
		heures = h;
		minutes = m;
	}

	// Cancer d'avoir des références vers ça ici. Mauvaise conception. 
	SpinnerModel heures;
	SpinnerModel minutes;

	public void run() {
		while(true) {
			try {
				Thread.sleep(1*1000);
			} catch(InterruptedException ie) {
				Thread.currentThread().interrupt();
				System.err.println(ie);
			}

			LocalDateTime time = LocalDateTime.now();

			LocalDateTime wake = LocalDateTime.of(time.getYear(), time.getMonthValue(), 
				time.getDayOfMonth(), (Integer)heures.getValue(), (Integer)minutes.getValue());

			// C'est tout un optimisation
			if (time.getMinute() == wake.getMinute()) {
				if (time.getHour() == wake.getHour()) {
					if (++tics > THRES) {
						buzzer();
						break;	// Sort de cette boucle pour ne pas buzzer plusieurs fois
					}
					continue;	// On fait revérifier quelques autres fois le temps
				}
			}

			tics = 0;
		}

		while(true) {
			try {
				Thread.sleep(60*60*1000);
			} catch(InterruptedException ie) {
				Thread.currentThread().interrupt();
				System.err.println(ie);
			}
		}
	}

	public void buzzer() {
		// http://soundbible.com/tags-alarm.html
		MediaPlayer a = new MediaPlayer(new Media(new File("alarme.mp3").toURI().toString()));
		a.setOnEndOfMedia(new Runnable() {
			public void run() {
				a.seek(Duration.ZERO);
			}
		});
		a.play();
	}
}

// Classe princiaple (GUI)
public class Alarme extends JFrame {
	public Alarme() {
		// Faire un paneau avec une disposition en grille
		GridLayout layout = new GridLayout(2,2);
		JPanel pan = new JPanel();
		pan.setLayout(layout);

		// 1
		JLabel lbl1 = new JLabel("Entrez heure:");
		pan.add(lbl1);

		// 2 - il est à noter qu'un spinner ne met pas à jour sa valeur tant que le focus n'as pas changé
		SpinnerModel heures = new SpinnerNumberModel(8, 0, 23, 1);
		JSpinner heuresBox = new JSpinner(heures);
		pan.add(heuresBox);

		// 3
		JLabel lbl3 = new JLabel("Entrez minutes:");
		pan.add(lbl3);

		// 4 - il est à noter qu'un spinner ne met pas à jour sa valeur tant que le focus n'as pas changé
		SpinnerModel minutes = new SpinnerNumberModel(30, 0, 59, 1);
		JSpinner minutesBox = new JSpinner(minutes);
		pan.add(minutesBox);

		// Place stuff inside the JFrame
		this.getContentPane().add(pan);
		this.pack();
		this.setVisible(true);

		// Muh threads
		Veille v = new Veille(heures, minutes);
		Thread t1 = new Thread(v);
		t1.start();
	}

	public static void main(String[] args) {
		Alarme window = new Alarme();
		window.setTitle("ALARME FATIGUANTE");
		window.setSize(300, 200);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
	}
}
