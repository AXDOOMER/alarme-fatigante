// Copyright (c) 2017, Alexandre-Xavier Labonté-Lamoureux
//
// Permission to use, copy, modify, and/or distribute this software for any
// purpose with or without fee is hereby granted, provided that the above
// copyright notice and this permission notice appear in all copies.
//
// THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH
// REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY
// AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
// INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
// LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE
// OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
// PERFORMANCE OF THIS SOFTWARE.


// Interface graphique
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.GridLayout;
import javax.swing.JOptionPane;

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
import javafx.scene.media.AudioClip;

// Classe d'un thread qui roule dans le fond
class Veille implements Runnable {

	int tics = 0;
	final int THRES = 3;

	public Veille(SpinnerModel h, SpinnerModel m) {
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
				JOptionPane.showMessageDialog(null, ie);
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
				JOptionPane.showMessageDialog(null, ie);
			}
		}
	}

	public void buzzer() {
		AudioClip clip = new AudioClip(new File("alarme.mp3").toURI().toString());
		clip.setCycleCount(AudioClip.INDEFINITE);
		clip.play();
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
