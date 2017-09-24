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


// Fichiers
import java.io.File;
import java.io.IOException;

// Date
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

// Son
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Clip;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.LineUnavailableException;

// Classe princiaple
public class AlarmeCmd {

	// Seuil avant de faire sonner l'alarme
	int tics = 0;
	final int THRES = 3;

	// Main
	public static void main(String[] args) {

		if (args.length > 0) {
			try {
				LocalTime temps = LocalTime.parse(args[0]);

				// Démarrer l'alarme
				AlarmeCmd alarme = new AlarmeCmd(temps);
			} catch(DateTimeParseException dtpe) {
				System.err.println("Le temps à été spécifié dans un format invalide.");
			}
		} else {
			System.err.println("Le temps où l'alarme doit sonner n'a pas été spécifié à la ligne de commande.");
		}
	}

	public AlarmeCmd(LocalTime temps) {
		while(true) {
			try {
				Thread.sleep(1*1000);
			} catch(InterruptedException ie) {
				Thread.currentThread().interrupt();
				System.err.println(ie);
			}

			// C'est tout un optimisation
			if (LocalTime.now().getMinute() == temps.getMinute()) {
				if (LocalTime.now().getHour() == temps.getHour()) {
					if (++tics > THRES) {
						buzzer();
						break;	// Sort de cette boucle pour ne pas déclencher le buzzer plusieurs fois
					}
					continue;	// On fait revérifier quelques autres fois le temps
				}
			}

			tics = 0;
		}

		while(true) {
			try {
				// Arrêter de sonner après 5 minutes
				Thread.sleep(5*60*1000);
				System.exit(0);
			} catch(InterruptedException ie) {
				Thread.currentThread().interrupt();
				System.err.println(ie);
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
			System.err.println(uafe);
		} catch (IOException ioe) {
			System.err.println(ioe);
		} catch (LineUnavailableException lue) {
			System.err.println(lue);
		} catch (IllegalArgumentException iae) {
			System.err.println(iae);
		}
	}
}
