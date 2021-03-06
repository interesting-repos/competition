package io;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import structures.cell.AbstractCell;
import structures.views.StateViewer;
import control.parameters.Parameters;

/**
 *
 * Copyright (c) 2013, David Bruce Borenstein.
 * 
 * This file is part of the source code for "Non-local interaction via diffusible resource 
 * prevents coexistence of cooperators and cheaters in a lattice model"
 * (PLOS ONE, Borenstein, et al. 2013).
 * 
 * This work is licensed under the Creative Commons 2.0 BY-NC license.
 * 
 * Attribute (BY) -- You must attribute the work in the manner specified 
 * by the author or licensor (but not in any way that suggests that they 
 * endorse you or your use of the work).
 * 
 * Noncommercial (NC) -- You may not use this work for commercial purposes.
 * 
 * For the full license, please visit:
 * http://creativecommons.org/licenses/by-nc/3.0/legalcode
 * 
 * 
 * This is really just the CatalystWriter and the DerivativeWriter
 * drawn side by side. I just duplicated the former and plunked
 * in the necessary code from the latter.
 * 
 * @author dbborens@princeton.edu
 *
 */
public class TwoPaneMapWriter {

	/* CONSTANTS */
	private static final int CELL_HEIGHT = 5;
	private static final int CELL_WIDTH = 5;
	
	/* STATE VARIABLES */
	
	private Parameters p;
	private String path;
	
	private int width;
	private int height;
	
	private String format;
	
	/* CONSTRUCTORS */
	public TwoPaneMapWriter(Parameters p, String basePath, String format) {
		this.p = p;
		this.format = format;
		
		String path = basePath + "/dual/";
		System.out.println(path);
		mkDir(path);
		
		this.path = path;
		
		width = 2 * CELL_WIDTH * p.W();
		height = CELL_HEIGHT * p.W();
	}
	
	private BufferedImage buildImage(StateViewer state) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
 		Graphics gfx = img.getGraphics();
 		
		buildCellMap(state, gfx);
		//buildSoluteMap(state, gfx);
		
		return img;
	}

	private void buildSoluteMap(StateViewer state, Graphics gfx) {
		int offset = p.W() * CELL_WIDTH;
		for (int i = 0; i < p.N(); i++) {

			float r = ((Double) state.getEnzyme().getScaled(i)).floatValue();	
			
			//int x = offset + i % p.W();
			int x = i % p.W();
			int y = p.W() - i / p.W() - 1;	// Graphics coordinates invert Y-axis
 				
			drawGridPoint(gfx, x, y, r, r, r);

		}
	}

	private void buildCellMap(StateViewer state, Graphics gfx) {
		for (int i = 0; i < p.N(); i++) {
			
			// Empty cells are black
			float r = 0;
			float g = 0;
			float b = 0;
			
			if (state.getColor()[i] == AbstractCell.CHEATER) {
				b = (float) Math.pow(0.75f * (float) state.getDerivatives().getScaled(i), 2);
				//g = (float) Math.pow(0.9f * b, 1.5d);
				g = 0.7f * b;
				r = g;
				
				b += 0.25f;
			} else if (state.getColor()[i] == AbstractCell.COOPERATOR) {
				r = (float) Math.pow(0.75f * (float) state.getDerivatives().getScaled(i), 2);
				//g = (float) Math.pow(0.9f * r, 1.5d);
				g = 0.7f * r;
				b = g;
				
				r += 0.25f;
			} else if (state.getColor()[i] == AbstractCell.DEAD) {
				r = 0.5f;
				g = 0.5f;
				b = 0.5f;
			} else if (state.getColor()[i] == AbstractCell.EMPTY) {
				g = 0.25f;
			}
			
			int x = i % p.W();
			int y = p.W() - i / p.W() - 1;	// Graphics coordinates invert Y-axis
 				
			drawGridPoint(gfx, x, y, r, g, b);
			float s;
			if (p.getProduction() < p.epsilon())
				s = 0f;
			else
				s = ((Double) state.getEnzyme().getScaled(i)).floatValue();
			
			drawGridPoint(gfx, x + p.W(), y, s, s, s);

		}
	}

	private void drawGridPoint(Graphics gfx, int x, int y,
			float r, float g, float b) {
		try {
			Color c = new Color(r, g, b);
			gfx.setColor(c);
			gfx.fillRect(x * CELL_WIDTH, y * CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);
		} catch (Exception ex) {
			System.out.println("Offending values: " + r + ", " + g + ", " + b);
			throw new RuntimeException(ex);
		}
	}

	/* PUBLIC METHODS */
	
	public void refresh(StateViewer state) {
		BufferedImage img = buildImage(state);
		export(img, state.getGillespie());
	}

	private void export(BufferedImage img, double gillespie) {
		String tStr = String.format(format, gillespie);
		
		File f = new File(path + tStr);
		try {
			ImageIO.write(img, "png", f);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public int getNominalWidth() {
		return width;
	}
	
	public int getNominalHeight() {
		return height;
	}
	
	private void mkDir(String pathStr) {
		File path = new File(pathStr);
		if (!path.exists()) {
			try {
				path.mkdir();
			} catch (Exception ex) {
				System.out.println("Could not create directory" + pathStr);
				throw new RuntimeException(ex);
			}			
		}
	}
}
