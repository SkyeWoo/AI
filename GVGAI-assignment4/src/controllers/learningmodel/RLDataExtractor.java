/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.learningmodel;

import org.w3c.dom.Attr;
import tools.*;
import core.game.Observation;
import core.game.StateObservation;

import java.io.FileWriter;
import java.util.*;

import ontology.Types;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @author yuy
 */
public class RLDataExtractor {
    public FileWriter filewriter;
    public static Instances s_datasetHeader = datasetHeader();

    public RLDataExtractor(String filename) throws Exception {

        filewriter = new FileWriter(filename + ".arff");
        filewriter.write(s_datasetHeader.toString());
        /*
                // ARFF File header
        filewriter.write("@RELATION AliensData\n");
        // Each row denotes the feature attribute
        // In this demo, the features have four dimensions.
        filewriter.write("@ATTRIBUTE gameScore  NUMERIC\n");
        filewriter.write("@ATTRIBUTE avatarSpeed  NUMERIC\n");
        filewriter.write("@ATTRIBUTE avatarHealthPoints NUMERIC\n");
        filewriter.write("@ATTRIBUTE avatarType NUMERIC\n");
        // objects
        for(int y=0; y<14; y++)
            for(int x=0; x<32; x++)
                filewriter.write("@ATTRIBUTE object_at_position_x=" + x + "_y=" + y + " NUMERIC\n");
        // The last row of the ARFF header stands for the classes
        filewriter.write("@ATTRIBUTE Class {0,1,2}\n");
        // The data will recorded in the following.
        filewriter.write("@Data\n");*/

    }

    public static Instance makeInstance(double[] features, int action, double reward) {
        // Action is
        features[872] = action;
//        features[191] = action;
        features[873] = reward;
//        features[192] = reward;
        Instance ins = new Instance(1, features);
        ins.setDataset(s_datasetHeader);
        return ins;
    }

    public static double[] featureExtract(StateObservation obs) {

        double[] feature = new double[874];  // 868 + 4 + 1(action) + 1(Q)

        // 448 locations
//        int[][] map = myExtractMap(obs);
        int[][] map = originalExtractMap(obs);

        for (int y = 0; y < 31; y++)
            for (int x = 0; x < 28; x++)
                feature[y * 28 + x] = map[x][y];

        // 4 states
        feature[868] = obs.getGameTick();
        feature[869] = obs.getAvatarSpeed();
        feature[870] = obs.getAvatarHealthPoints();
        feature[871] = obs.getAvatarType();

        return feature;
    }

    private static int[][] originalExtractMap(StateObservation obs) {
        int[][] map = new int[28][31];
        // Extract features
        LinkedList<Observation> allobj = new LinkedList<>();
        if (obs.getImmovablePositions() != null)
            for (ArrayList<Observation> l : obs.getImmovablePositions()) allobj.addAll(l);
        if (obs.getMovablePositions() != null)
            for (ArrayList<Observation> l : obs.getMovablePositions()) allobj.addAll(l);
        if (obs.getNPCPositions() != null)
            for (ArrayList<Observation> l : obs.getNPCPositions()) allobj.addAll(l);

        for (Observation o : allobj) {
            Vector2d p = o.position;
            int x = (int) (p.x / 20); //square size is 20 for pacman
            int y = (int) (p.y / 20);
            map[x][y] = o.itype;
        }

        return map;
    }

    private static int[][] myExtractMap(StateObservation obs) {
        // 448 locations
        int[][] map = new int[28][31];
        // Extract features
        LinkedList<Observation> allobj = new LinkedList<>();
        if (obs.getImmovablePositions() != null)
            for (ArrayList<Observation> l : obs.getImmovablePositions()) allobj.addAll(l);
        if (obs.getMovablePositions() != null)
            for (ArrayList<Observation> l : obs.getMovablePositions()) allobj.addAll(l);
        if (obs.getNPCPositions() != null)
            for (ArrayList<Observation> l : obs.getNPCPositions())
                allobj.addAll(l);
        if (obs.getPortalsPositions() != null)
            for (ArrayList<Observation> l : obs.getPortalsPositions()) allobj.addAll(l);
        if (obs.getResourcesPositions() != null)
            for (ArrayList<Observation> l : obs.getResourcesPositions()) allobj.addAll(l);

        for (Observation o : allobj) {
            Vector2d p = o.position;
            int x = (int) (p.x / 20); //square size is 20 for pacman
            int y = (int) (p.y / 20);
            map[x][y] = o.itype;
        }

        // Add avatar's position
        Vector2d avatarPos = obs.getAvatarPosition();
        int avatar_x = (int) (avatarPos.x / 20);
        int avatar_y = (int) (avatarPos.y / 20);
        map[avatar_x][avatar_y] = obs.getAvatarType();

        return map;
    }

    private static double[] myExtractFeature(StateObservation obs) {
        List<Double> feature = new ArrayList<>(166);

        int[][] map = myExtractMap(obs);

        // Get avatar's position
        Vector2d avatarPos = obs.getAvatarPosition();
        int avatar_x = (int) (avatarPos.x / 20);
        int avatar_y = (int) (avatarPos.y / 20);

        // Horizontal and vertical line
        for (int i = 0; i < 28; i++) {
            feature.add(1. * map[i][avatar_y]);
        }
        for (int i = 0; i < 31; i++) {
            if (i == avatar_y)
                continue;  // Avoid repeat
            feature.add(1. * map[avatar_x][i]);
        }

        // 11x11 grids
        for (int i = avatar_x - 5; i <= avatar_x + 5; i++) {
            for (int j = avatar_y - 5; j <= avatar_y + 5; j++) {
                if (i < 0 || i >= 28 || j < 0 || j >= 31)
                    feature.add(0.);  // Just set it as a wall
                else
                    feature.add(1. * map[i][j]);
            }
        }

        // Get enemies' position
        if (obs.getNPCPositions() != null) {
            for (ArrayList<Observation> l : obs.getNPCPositions()) {
                l.forEach(e -> {
                    Vector2d p = e.position;
                    feature.add(p.x / 20);
                    feature.add(p.y / 20);
                });
            }
            // If the enemy is frozen, set them to 0
            while (feature.size() <= 187)
                feature.add(0.);
        } else {
            for (int i = 0; i < 8; i++) {
                feature.add(0.);
            }
        }

        // Transform to array
        assert feature.size() == 187;
        double[] result = new double[193];  // 187 + 4 + 1(action) + 1(Q)
        for (int i = 0; i < 187; i++) {
            result[i] = feature.get(i);
        }

        result[187] = obs.getGameTick();
        result[188] = obs.getAvatarSpeed();
        result[189] = obs.getAvatarHealthPoints();
        result[190] = obs.getAvatarType();

        return result;
    }

    static Instances myDatasetHeader() {
        if (s_datasetHeader != null)
            return s_datasetHeader;

        FastVector attInfo = new FastVector();
        // Horizontal and vertical map information
        for (int i = 0; i < 28; i++) {
            Attribute att = new Attribute("Horizontal object x=" + i);
            attInfo.addElement(att);
        }
        for (int i = 0; i < 30; i++) {
            Attribute att = new Attribute("Vetical object y=" + i);
            attInfo.addElement(att);
        }

        // 11 * 11
        for (int i = 0; i < 121; i++) {
            String position = "object at relative_x=" + (i % 11) + " relative_y=" + (i / 11);
            attInfo.addElement(new Attribute(position));
        }

        // Enemy
        for (int i = 1; i <= 4; i++) {
            attInfo.addElement(new Attribute("enemy" + i + " x"));
            attInfo.addElement(new Attribute("enemy" + i + " y"));
        }

        Attribute att = new Attribute("GameTick");
        attInfo.addElement(att);
        att = new Attribute("AvatarSpeed");
        attInfo.addElement(att);
        att = new Attribute("AvatarHealthPoints");
        attInfo.addElement(att);
        att = new Attribute("AvatarType");
        attInfo.addElement(att);
        //action
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
        instances.setClassIndex(instances.numAttributes() - 1);  // Q value is the label

        return instances;
    }

    public static Instances datasetHeader() {

        if (s_datasetHeader != null)
            return s_datasetHeader;

        FastVector attInfo = new FastVector();
        // 448 locations
        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 31; x++) {
                Attribute att = new Attribute("object_at_position_x=" + x + "_y=" + y);
                attInfo.addElement(att);
            }
        }
        Attribute att = new Attribute("GameTick");
        attInfo.addElement(att);
        att = new Attribute("AvatarSpeed");
        attInfo.addElement(att);
        att = new Attribute("AvatarHealthPoints");
        attInfo.addElement(att);
        att = new Attribute("AvatarType");
        attInfo.addElement(att);
        //action
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
        instances.setClassIndex(instances.numAttributes() - 1);  // Q value is the label

        return instances;
    }

}
