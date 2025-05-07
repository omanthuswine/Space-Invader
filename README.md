# Space_Invader_Game

## Overview

Space Shooter is a lightweight 2D game built with JavaFX. Pilot your spaceship, dodge enemy fire, collect power-ups, and defeat waves of enemies—including challenging boss encounters—as you aim for the highest score.

## User Interface

### Demo GIF
<p align="center" style="cursor: pointer">
    <img src="img/demo.gif" alt="Space Shooter Game Demo" width="60%"/>
</p>

### Start Screen

<p align="center" style="cursor: pointer">
    <img src="img/start.png" alt="Space Shooter Game Start Screen" width="60%"/>
</p>

### Gameplay

<p align="center" style="cursor: pointer">
    <img src="img/gameplay.png" alt="Space Shooter Game Interface" width="60%"/>
</p>

### Instructions

<p align="center" style="cursor: pointer">
    <img src="img/instructions.png" alt="Space Shooter Game Instructions" width="60%"/>
</p>

### Losing Screen

<p align="center" style="cursor: pointer">
    <img src="img/losing.png" alt="Space Shooter Game Losing Screen" width="60%"/>
</p>

## Game Objects

Design your class/interface hierarchy to maximize inheritance, polymorphism, and encapsulation.
- Player: the player’s spaceship; can move and shoot bullets
- Enemy: basic alien; moves according to a simple pattern
- BossEnemy: powerful enemy with a more complex AI movement/shooting pattern
- Bullet: projectile fired by the Player
- EnemyBullet: projectile fired by Enemies/BossEnemy
- PowerUp: appears randomly; grants bonuses such as increased fire rate or extra lives..

## Features

- Control a spaceship using keyboard inputs (A, W, S, D, or arrow keys) to move and SPACE to shoot.
- Enemies and boss enemies spawn at intervals, increasing the game's difficulty.
- Collect power-ups to boost your abilities.
- Score tracking and display, with the game increasing in difficulty as your score rises.
- Lives system where players lose a life if an enemy reaches the bottom of the screen or collides with the player's spaceship.
- Reset mechanism to start over once all lives are lost.
- Sound effects for shooting, enemy hits, and power-up collection.
- JavaDoc documentation for the game's classes and methods.

## General Requirements

- Implement at least one complete level (win by destroying all waves of enemies).
- Provide a Start, Pause, and Game Over UI; display Score and Lives.
- Initialize the level layout and enemy in code (you may hard-code their positions directly).
- Ensure smooth performance.

## How to Play

- Use the A, W, S, D keys or arrow keys to move the spaceship.
- Press SPACE to shoot at enemies.
- Avoid letting enemies reach the bottom of the screen or colliding with them.
- Collect power-ups to enhance your capabilities and increase your score.
- The game ends when all lives are lost, but you can start over by resetting the game.

## Your Tasks

### Mandatory Tasks (+8 pts)

- Inheritance Design: Define an OOP hierarchy for all game classes +2 pts
- Player Controls: Implement player movement and shooting via ←/→/↑/↓ keys and SPACE +1 pt
- Enemy Spawning & Movement: Automatically spawn and move Enemy objects (including speed and pattern)  +1 pt
- Projectile Management: Create and manage Bullet and EnemyBullet objects +1 pts
- Collisions & Explosions: Detect when bullets hit objects (enemies, player), mark them dead, and display explosion effects  +1 pt
- PowerUps: Randomly spawn PowerUp objects; upon collection, update player attributes (e.g., fireRate++, lives++) +1 pt
- Basic UI: Implement Start/Pause/Game Over screens, display Score & Lives, and allow level retry on Game Over +2 pts

### Advanced Tasks (+2 pts)

- Player AI (Auto-Play): Implement an automated player that shoots and dodges using heuristics or search algorithms +1 pt
- Multiplayer (LAN/Internet): Develop a client–server mode for two players to play cooperatively or in PvP +1 pt
- ... (Other creative ideas will be evaluated and awarded corresponding points).

## Prerequisites

- JDK 11 or higher.
- JavaFX SDK (version compatible with your JDK).

Good luck, and have fun building your Space Invader.
