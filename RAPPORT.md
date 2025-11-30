# RAPPORT


Jeg refaktorerede floklogikken ud af Boid til BahavoirStrategy med standardstrategien i FlockBehavior og muligheden for at tildele strategier pr. boid type via FlockSimulation. Derudover tilføjede jeg en ny strategi - WanderBehavior - og registrerede den som BoidType.WANDERER. Jeg har lagt en UI-skyder ind til at blande wanderers med standardboids, så forskellen kan ses i simolatioren.

Wanderers følger flocking reglerne, men der tilføjes en lille random bevægelse (jitter) hvert tick til enten venstre eller højre. Man kan styre hvor mange der skal være med den nye slider. 

Jeg har tilføjet Microbench, som måler simulationstrinnet på et 1200x800 kort, 1500 boids, nabo-radios 60, warmup sat til 75 iterationer, måling sat til 300 iterationer. Wander sættes til 0 for at holde adfærden ens.
Derved afspejler mplingen kun spartial index-performance. Derudover bruger SpartialHash cellestørrelse 60. 

De 1500 boids er nok til at Naive 0(n^2) bliver tungere, mens de andre skalerer.


- Resultater (ms pr. iteration. Gennemsnit over 300 iterationer på min kørsel):
    - Naive O(n²): 8.481
    - KD-Tree: 4.190
    - Spatial Hashing: 3.766
    - QuadTree: 4.536