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
import javax.swing.JOptionPane;
import java.awt.GridLayout;

// Fichiers
import java.io.File;
import java.io.IOException;

// Boîtes numériques
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

// Date
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Son
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Clip;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.LineUnavailableException;

// Classe qui veille jusqu'à déclancher l'alarme
class Cadran implements Runnable {

	int tics = 0;
	final int THRES = 3;

	public Cadran(SpinnerModel h, SpinnerModel m) {
		// Beurk!
		heures = h;
		minutes = m;
	}

	// Mauvaise conception...
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
		try {
			AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File("alarme.au"));
			AudioFormat format = inputStream.getFormat();
			DataLine.Info info = new DataLine.Info(Clip.class, format);
			Clip clip = (Clip)AudioSystem.getLine(info);
			clip.open(inputStream);
			clip.loop(Clip.LOOP_CONTINUOUSLY);
		} catch (UnsupportedAudioFileException uafe) {
			JOptionPane.showMessageDialog(null, uafe);
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(null, ioe);
		} catch (LineUnavailableException lue) {
			JOptionPane.showMessageDialog(null, lue);
		} catch (java.lang.IllegalArgumentException iae) {
			JOptionPane.showMessageDialog(null, iae);
		}
	}
}

// Classe princiaple (GUI)
public class Alarme extends JFrame {
	public Alarme() {
		// Faire un paneau avec une disposition en grille
		GridLayout layout = new GridLayout(2,2);
		JPanel pan = new JPanel();
		pan.setLayout(layout);

		// Ajout des composantes dans la grille
		JLabel lblHeures = new JLabel("Entrez heure:");
		pan.add(lblHeures);

		SpinnerModel heures = new SpinnerNumberModel(8, 0, 23, 1);
		JSpinner spnrHeures = new JSpinner(heures);
		pan.add(spnrHeures);

		JLabel lblMinutes = new JLabel("Entrez minutes:");
		pan.add(lblMinutes);

		SpinnerModel minutes = new SpinnerNumberModel(30, 0, 59, 1);
		JSpinner spnrMinutes = new JSpinner(minutes);
		pan.add(spnrMinutes);

		// Mettre les composantes dans le JFrame
		this.getContentPane().add(pan);
		this.pack();
		this.setVisible(true);

		// Un fil d’exécution!
		Cadran cadran = new Cadran(heures, minutes);
		Thread threadCadran = new Thread(cadran);
		threadCadran.start();
	}

	public static void main(String[] args) {
		Alarme window = new Alarme();
		window.setTitle("ALARME FATIGUANTE");
		window.setSize(300, 200);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
	}
}
