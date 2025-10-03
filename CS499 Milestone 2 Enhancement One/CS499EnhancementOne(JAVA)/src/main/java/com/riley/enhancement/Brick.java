package com.riley.enhancement;

import static org.lwjgl.opengl.GL11.*;

enum BRICKTYPE { REFLECTIVE, DESTRUCTABLE };
enum ONOFF { ON, OFF };

public class Brick {
	float red, green, blue;
	double x, y, width;
	BRICKTYPE brick_type;
	ONOFF onoff;
	int hitPoints;
	int maxHitPoints;
	int hp;
	
	public Brick(BRICKTYPE bt, double xx, double yy, double ww, float rr, float gg, float bb, int hp) {
		this.brick_type = bt;
		this.x = xx; 
		this.y = yy; 
		this.width = ww;
		this.red = rr;
		this.green = gg;
		this.blue = bb;
		this.onoff = ONOFF.ON;
		this.hitPoints = hp;
		this.maxHitPoints = hp;
	}
	
	void drawBrick() {
		if (onoff == ONOFF.ON)
		{
			double halfside = width / 2;

			// Unique values for DESTRUCTABLE brick_type
			if (brick_type == BRICKTYPE.DESTRUCTABLE) {
				float hp = (float)hitPoints / maxHitPoints;
				float r = 1.0f - hp;
				float g = hp;
				float b = 0.0f;

				glColor3f(r, g, b);
			}
			else {
				glColor3d(red, green, blue);
			}
			
			//draws the brick
			glBegin(GL_POLYGON);

			glVertex2d(x + halfside, y + halfside);
			glVertex2d(x + halfside, y - halfside);
			glVertex2d(x - halfside, y - halfside);
			glVertex2d(x - halfside, y + halfside);

			glEnd();
		}
	}
}