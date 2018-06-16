/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.learningmodel;

import java.io.FileWriter;
import java.util.ArrayList;

import core.game.Observation;
import core.game.StateObservation;
import tools.Vector2d;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author yuy
 */
public class RLDataExtractor {
	public FileWriter filewriter;
	public static Instances s_datasetHeader = datasetHeader();

	public RLDataExtractor(String filename) throws Exception {

		filewriter = new FileWriter(filename + ".arff");
		filewriter.write(s_datasetHeader.toString());
		/*
		 * // ARFF File header filewriter.write("@RELATION AliensData\n"); // Each row
		 * denotes the feature attribute // In this demo, the features have four
		 * dimensions. filewriter.write("@ATTRIBUTE gameScore  NUMERIC\n");
		 * filewriter.write("@ATTRIBUTE avatarSpeed  NUMERIC\n");
		 * filewriter.write("@ATTRIBUTE avatarHealthPoints NUMERIC\n");
		 * filewriter.write("@ATTRIBUTE avatarType NUMERIC\n"); // objects for(int y=0;
		 * y<14; y++) for(int x=0; x<32; x++)
		 * filewriter.write("@ATTRIBUTE object_at_position_x=" + x + "_y=" + y +
		 * " NUMERIC\n"); // The last row of the ARFF header stands for the classes
		 * filewriter.write("@ATTRIBUTE Class {0,1,2}\n"); // The data will recorded in
		 * the following. filewriter.write("@Data\n");
		 */

	}

	public static Instance makeInstance(double[] features, int action, double reward) {
		features[s_datasetHeader.numAttributes() - 2] = action;
		features[s_datasetHeader.numAttributes() - 1] = reward;
		Instance ins = new Instance(1, features);
		ins.setDataset(s_datasetHeader);
		return ins;
	}

	private static boolean isValid(StateObservation obs, int delta_x, int delta_y) {
		ArrayList<Observation> grid[][] = obs.getObservationGrid();
		Vector2d avatarPos = obs.getAvatarPosition();
		int x = (int) (avatarPos.x + delta_x * 20);
		int y = (int) (avatarPos.y + delta_y * 20);
		int xLen = grid.length;
		int yLen = grid[0].length;
		x = ((int) x + xLen) % xLen;
		y = ((int) y + yLen) % yLen;
		if (grid[x][y].size() == 1 && grid[x][y].get(0).itype == 0)
			return false;

		return true;
	}

	private static int getNearestDir(StateObservation obs, Vector2d spritePos) {
		Vector2d avatarPos = obs.getAvatarPosition();
		if (avatarPos.x > spritePos.x && isValid(obs, -1, 0))
			return 0; // left
		if (avatarPos.x < spritePos.x && isValid(obs, 1, 0))
			return 1; // right
		if (avatarPos.y > spritePos.y && isValid(obs, 0, 1))
			return 2; // down
		if (avatarPos.y < spritePos.y && isValid(obs, 0, -1))
			return 3; // up

		return 0;
	}

	private static double Manhattan(Vector2d avatarPos, Vector2d spritePos) {
		return Math.abs(avatarPos.x - spritePos.x) + Math.abs(avatarPos.y - spritePos.y);
	}

	public static double[] featureExtract(StateObservation obs) {

		double[] feature = new double[s_datasetHeader.numAttributes()];

		// Extract features

		// immovable itype: 0-wall, 3-mushroom, 4-road
		Vector2d avatarPos = obs.getAvatarPosition();
		double mindist = Double.MAX_VALUE;

		if (obs.getImmovablePositions() != null)
			for (ArrayList<Observation> l : obs.getImmovablePositions())
				for (Observation o : l)
					if (o.itype == 3) {// mushroom
						double man = Manhattan(avatarPos, o.position);
						if (mindist > man) {
							mindist = man;
							feature[0] = o.position.x;
							feature[1] = o.position.y;
						}
					}
		feature[2] = mindist;

		int i = 3;
		// NPC itype: 14 17 20 23-enemy
		mindist = Double.MAX_VALUE;
		if (obs.getNPCPositions() != null)
			for (ArrayList<Observation> l : obs.getNPCPositions())
				for (Observation o : l)
					switch (o.itype) {
					case 14:
					case 17:
					case 20:
					case 23:
						double man = Manhattan(avatarPos, o.position);
						feature[i++] = o.position.x;
						feature[i++] = o.position.y;
						feature[i++] = man;
						mindist = Math.min(mindist, man);
						break;
					default:
						break;
					}

		// System.out.println(i); // it should be 15

		mindist = Double.MAX_VALUE;
		// resource itype: 5-diamond
		if (obs.getResourcesPositions() != null)
			for (ArrayList<Observation> l : obs.getResourcesPositions())
				for (Observation o : l)
					if (o.itype == 5) {// diamond
						double man = Manhattan(avatarPos, o.position);
						if (mindist > man) {
							feature[15] = o.position.x;
							feature[16] = o.position.y;
							mindist = man;
						}
					}
		feature[17] = mindist;
		feature[18] = obs.getGameTick();
		feature[19] = obs.getAvatarType();
		return feature;
	}

	public static Instances datasetHeader() {

		if (s_datasetHeader != null)
			return s_datasetHeader;

		FastVector attInfo = new FastVector();
		Attribute att;

		att = new Attribute("nearestMushroomX");
		attInfo.addElement(att);
		att = new Attribute("nearestMushroomY");
		attInfo.addElement(att);
		att = new Attribute("nearestMushroom");
		attInfo.addElement(att);

		for (int i = 0; i < 4; i++) {
			att = new Attribute("NPC" + i + "_X");
			attInfo.addElement(att);
			att = new Attribute("NPC" + i + "_Y");
			attInfo.addElement(att);
			att = new Attribute("NPC" + i + "_Manhattan");
			attInfo.addElement(att);
		}

		att = new Attribute("nearestDiamondX");
		attInfo.addElement(att);
		att = new Attribute("nearestDiamondY");
		attInfo.addElement(att);
		att = new Attribute("nearestDiamond");
		attInfo.addElement(att);

		att = new Attribute("GameTick");
		attInfo.addElement(att);
		att = new Attribute("AvatarType");
		attInfo.addElement(att);

		// action
		FastVector actions = new FastVector();
		actions.addElement("0");
		actions.addElement("1");
		actions.addElement("2");
		actions.addElement("3");
		att = new Attribute("actions", actions);
		attInfo.addElement(att);
		// Q value
		att = new Attribute("Qvalue");
		attInfo.addElement(att);

		Instances instances = new Instances("PacmanQdata", attInfo, 0);
		instances.setClassIndex(instances.numAttributes() - 1);

		return instances;
	}

}
