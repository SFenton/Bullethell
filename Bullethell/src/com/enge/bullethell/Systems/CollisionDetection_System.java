package com.enge.bullethell.Systems;

import java.util.ArrayList;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.audio.Sound;
import com.enge.bullethell.Bullethell;
import com.enge.bullethell.Vector2;
import com.enge.bullethell.Components.Bullet_Component;
import com.enge.bullethell.Components.Health_Component;
import com.enge.bullethell.Components.Hitbox_Component;
import com.enge.bullethell.Components.Owner_Component;
import com.enge.bullethell.Components.Position_Component;
import com.enge.bullethell.Components.Score_Component;

/**
 * System that determines whether two entities are intersecting and
 * reacts appropriately.
 * @version 09.04.2013
 */
public class CollisionDetection_System extends EntityProcessingSystem {

	private ArrayList<Entity> entities;

	@Mapper ComponentMapper<Position_Component> positionM;
	@Mapper ComponentMapper<Hitbox_Component> hitboxM;
	@Mapper ComponentMapper<Bullet_Component> bulletM;
	@Mapper ComponentMapper<Owner_Component> ownerM;
	@Mapper ComponentMapper<Health_Component> healthM;
	@Mapper ComponentMapper<Score_Component> scoreM;
	private long lastUpdated;
	private Sound collision;
	private Sound death;
	private Sound enemyCollision;
	private Sound explosion;

	/**
	 * Constructor for the class.
	 * @param explosion 
	 * @param enemyCollision 
	 * @param death 
	 * @param collision 
	 * @param aspect aspect
	 */
    @SuppressWarnings("unchecked")
	public CollisionDetection_System(Sound collision, Sound death, Sound enemyCollision, Sound explosion) {
        super(Aspect.getAspectForAll(Position_Component.class,
        		Hitbox_Component.class, Owner_Component.class));
        		//.getAspectForOne(Health_Component.class, Score_Component.class));
        lastUpdated = System.currentTimeMillis();
        entities = new ArrayList<Entity>();
        this.collision = collision;
        this.death = death;
        this.enemyCollision = enemyCollision;
        this.explosion = explosion;
    }

    /**
     * Compares two entities' hitboxes and determines if they are intersecting.
     * @param entity1 The first entity to compare.
     * @param entity2 The second entity to compare.
     * @return Whether or not ehe entities are intersecting.
     */
    public boolean isIntersecting(Entity entity1, Entity entity2)
    {
    	int halfWidth1 = hitboxM.get(entity1).width / 2;
    	int halfHeight1 = hitboxM.get(entity1).height / 2;
    	int halfWidth2 = hitboxM.get(entity2).width / 2;
    	int halfHeight2 = hitboxM.get(entity2).height / 2;

    	Vector2 position1 = positionM.get(entity2).position;
    	Vector2 position2 = positionM.get(entity1).position;

    	if (position1.x - halfWidth1 < position2.x + halfWidth2 && position1.x + halfWidth1 > position2.x - halfWidth2 &&
    		position1.y - halfHeight1 < position2.y + halfHeight2 && position1.y + halfHeight1 > position2.y - halfHeight2) {
    		return true;
    	}
    	return false;
    }

    /**
     * Compares two entities, one with a hitbox and one that is treated as a point
     * and returns whether the point is in the hitbox.
     * @param pointEntity The entity treated as a point.
     * @param hitboxEntity The entity with the hitbox.
     * @return Whether or not the pointEntity is inside the hitboxEntity.
     */
    public boolean isInHitbox(Entity pointEntity, Entity hitboxEntity) {
    	Vector2 pointPosition = positionM.get(pointEntity).position;

    	Vector2 hitboxPosition = positionM.get(hitboxEntity).position;
    	int halfWidth = hitboxM.get(hitboxEntity).width / 2;
    	int halfHeight = hitboxM.get(hitboxEntity).height / 2;

    	if (pointPosition.y > hitboxPosition.y + halfHeight || pointPosition.y < hitboxPosition.y - halfHeight
    			|| pointPosition.x > hitboxPosition.x + halfWidth || pointPosition.x < hitboxPosition.x - halfWidth) {
    		return false;
    	}

		return true;
    }

    /**
     * Processes all given entities versus every other entity in the list.
     * @param e Unused.
     */
    @Override
    protected void process(Entity e) {
    	if (System.currentTimeMillis() - lastUpdated > 150) {
    		lastUpdated = System.currentTimeMillis();

    		Entity entity;
    		Entity secondEntity;

    		for (int i = 0; i < entities.size(); i++) {
    			entity = entities.get(i);
    			boolean isShip = scoreM.has(entity);

    			for (int j = i+1; j < entities.size(); j++)
    			{
    				secondEntity = entities.get(j);
    				if (entity != secondEntity)
    				{
    					int owner1 = ownerM.get(entity).owner;
    					int owner2 = ownerM.get(secondEntity).owner;
    					//If both are bullets, let them pass
    					//if (!scoreM.has(entity) && !scoreM.has(secondEntity))
    					//{
    					//Do nothing it's fine
    					//}

    					//If one is a bullet and the other is not, cases
    					if (isShip && !scoreM.has(secondEntity))
    					{
    						if (isInHitbox(secondEntity, entity)) {
    							//If the ship is enemy and the bullet is enemy, do nothing
    							//if (owner1 == 0 && owner2 == 0)
    							//{
    							//Do nothing; enemy bullets should not affect enemies
    							//}

    							//If the ship is enemy and the bullet is player, decrement enemy health
    							if (owner1 == 0 && owner2 == 1)
    							{
    								healthM.get(entity).health -= 1;
    								if (healthM.get(entity).health <= 0)
    								{
    									Bullethell.score += scoreM.get(entity).score;
    									entity.deleteFromWorld();
    									entities.remove(i);
    									explosion.play();
    								}
    								secondEntity.deleteFromWorld();
    								entities.remove(j);
    							}

    							//If the ship is player and the bullet is enemy, decrement player health
    							else if (owner1 == 1 && owner2 == 0)
    							{
    								//Decrement by 5?  Why not
    								healthM.get(entity).health -= 5;
    								if (healthM.get(entity).health <= 0)
    								{
    									//End the game
    									entity.deleteFromWorld();
    									entities.remove(i);
    									explosion.play();
    									break;
    								}
    								secondEntity.deleteFromWorld();
    								entities.remove(j);
    							}
    						}
    					}

    					//Now reverse it
    					else if (!isShip && scoreM.has(secondEntity))
    					{
    						if (isInHitbox(entity, secondEntity)) {
    							//If the bullet is enemy and the ship is enemy, do nothing
    							//if (owner1 == 0 && owner2 == 0)
    							//{
    							//Do nothing; enemy bullets should not affect enemies
    							//}

    							//If the bullet is enemy and the ship is friendly
    							if (owner1 == 0 && owner2 == 1)
    							{
    								healthM.get(secondEntity).health -= 1;
    								if (healthM.get(secondEntity).health <= 0)
    								{
    									//End the game
    									secondEntity.deleteFromWorld();
    									entities.remove(j);
    									death.play();
    									break;
    								}
    								entity.deleteFromWorld();
    								entities.remove(i);
    							}

    							//If the bullet is player and the ship is enemy, decrement
    							else if (owner1 == 1 && owner2 == 0)
    							{
    								//Decrement by 1?  Why not
    								healthM.get(secondEntity).health -= 1;
    								if (healthM.get(secondEntity).health <= 0)
    								{
    									Bullethell.score += scoreM.get(secondEntity).score;
    									secondEntity.deleteFromWorld();
    									entities.remove(j);
    									explosion.play();
    								}
    								entity.deleteFromWorld();
    								entities.remove(i);
    							}
    						}
    					}

    					//If both are ships
    					else if (isShip && scoreM.has(secondEntity))
    					{    						
    						if (isIntersecting(entity, secondEntity)) {
    							//if (owner1 == 1 && owner2 == 1)
    							//{
    							//Do nothing; we don't care if enemy ships interact
    							//}

    							if (owner1 == 1 && owner2 == 0)
    							{
    								healthM.get(entity).health -= 1;
    								if (healthM.get(entity).health <= 0)
    								{
    									Bullethell.score += scoreM.get(entity).score;
    									world.deleteEntity(entity);
    									entities.remove(entity);
    									explosion.play();
    								}
    								healthM.get(secondEntity).health -= 1;
    								if (healthM.get(secondEntity).health <= 0)
    								{
    									//End the game?
    									world.deleteEntity(secondEntity);
    									entities.remove(secondEntity);
    									death.play();
    									break;
    								}
    							}
    							else if (owner1 == 0 && owner2 == 1)
    							{
    								healthM.get(entity).health -= 1;
    								if (healthM.get(entity).health <= 0)
    								{
    									//End the game?
    									world.deleteEntity(entity);
    									entities.remove(entity);
    									death.play();
    									break;
    								}
    								healthM.get(secondEntity).health -= 1;
    								if (healthM.get(secondEntity).health <= 0)
    								{
    									//End the game?
    									Bullethell.score += scoreM.get(secondEntity).score;
    									world.deleteEntity(secondEntity);
    									entities.remove(secondEntity);
    									explosion.play();
    								}
    							}
    						}
    					}
    				}
    			}
    		}
    	}
    }

    /**
     * Inserts an entity into the Array List.
     * @param entity The entity to insert.
     */
    @Override
    protected void inserted(Entity entity) {
    	entities.add(entity);
    }

    /**
     * Removes an entity from the list.
     * @param entity The entity to remove.
     */
    @Override
    protected void removed(Entity entity) {
    	entities.remove(entity);
    }
}
