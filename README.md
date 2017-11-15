# Job Ad Classification
Multi-Label Classification for Job Ads

Code for Multi-Label Classification Job Ads into focuses as part of my Bachelor's Thesis
  
____________________________________________________________________________________

Das vorliegende Framework dient dazu, Stellenausschreibungen mithilfe von Trainingsdaten in Schwerpunkte zu klassifizieren oder die Klassifikation zu evaluieren. Desweiteren ist es möglich, die Trainingsdaten im Hinblick auf die Verteilung ihrer Schwerpunkte zu analysieren. Außerdem können vorausgesetzte Studienfächer und Abschlüsse aus Stellenausschreibungen extrahiert werden. 
Alle ausführbaren Klassen befinden sich im package src/main/java/de/uni_koeln/spinfo/ml_classification/applications

Um die Klasse SingleExperimentExecution auszuführen, werden folgende Dateien benötigt:
- Trainingsdaten (.xlsx)
- Liste mit möglichen Schwerpunkten (.xlsx)
- Liste mit möglichen Studienfächern (.xlsx)
- Liste mit möglichen Abschlüssen (.xlsx)
- Konfigurationsdatei (ml_classification/configurations.txt)

In der Datei ml_classification/configurations_manual.pdf wird beschrieben, welche Werte als Konfigurationen zulässig sind. In der Konfigurationsdatei lassen sich zudem die Pfade zu den anderen vier Dateien angeben.

_____________________________________________________________________________________

Die Klasse JobAdClassificationApp benötigt zu den genannten Dateien noch eine .xlsx-Datei mit den zu klassifizierenden Daten.


