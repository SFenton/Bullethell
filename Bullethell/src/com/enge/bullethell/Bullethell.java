package com.enge.bullethell;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.enge.bullethell.Components.Health_Component;
import com.enge.bullethell.Components.Position_Component;
import com.enge.bullethell.Components.Sprite_Component;
import com.enge.bullethell.Entities.ShipFactory_Entity;
import com.enge.bullethell.Systems.CollisionDetection_System;
import com.enge.bullethell.Systems.InputSystem;
import com.enge.bullethell.Systems.Movement_System;
import com.enge.bullethell.Systems.Render_System;

public class Bullethell implements ApplicationListener {
	private OrthographicCamera camera;
	private World world;
	private Render_System renderSystem;
	public static int score = 0;
	public static Entity player;
	
	private Entity enemy; 
	
	@Override
	public void create() {
		world = new World();
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		
		camera = new OrthographicCamera();
		renderSystem = new Render_System(camera);
		world.setSystem(renderSystem);
		world.setSystem(new CollisionDetection_System(world));
		world.setSystem(new Movement_System());
		
		player = ShipFactory_Entity.createPlayer(world, new Vector2(0, 0), 0);
		enemy = ShipFactory_Entity.createEnemy1(world, new Vector2(100, 600), new Vector2(0, -1), 0);
		
		
		
		world.initialize();
		Gdx.input.setInputProcessor(new InputSystem(world, camera));
	}

	@Override
	public void dispose() {
		//Clean up
	}

	@Override
	public void render() {
		world.setDelta(Gdx.graphics.getDeltaTime());
		world.process();
	}

	@Override
	public void resize(int width, int height) {
		renderSystem.resize(width, height);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}
