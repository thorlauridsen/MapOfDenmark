package bfst21.vector;

import bfst21.vector.osm.ExtendedWay;
import bfst21.vector.osm.Node;
import bfst21.vector.osm.Relation;
import bfst21.vector.osm.Way;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;


public class XmlParser {

    public MapData loadOSM(String filename) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
        return loadOSM(new FileInputStream(filename));
    }

    public MapData loadOSM(InputStream input) throws XMLStreamException, FactoryConfigurationError {

        XMLStreamReader reader = XMLInputFactory
            .newInstance()
            .createXMLStreamReader(new BufferedInputStream(input));

        Way way = null;
        Relation relation = null;
        ExtendedWay extendedWay = null;

        boolean checkingRelation = false;

        LongIndex idToNode = new LongIndex();
        LongIndex idToWay = new LongIndex();
        LongIndex idToRelation = new LongIndex();

        List<Drawable> shapes = new ArrayList<>();
        List<Drawable> buildings = new ArrayList<>();
        List<Drawable> extendedWays = new ArrayList<>();
        List<Way> coastlines = new ArrayList<>();
        List<Drawable> islands;

        float minx = 0, miny = 0, maxx = 0, maxy = 0;
        boolean isCoastline = false, isBuilding = false;

        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "bounds":
                            minx = getFloat(reader, "minlon");
                            maxx = getFloat(reader, "maxlon");
                            maxy = getFloat(reader, "minlat") / -0.56f;
                            miny = getFloat(reader, "maxlat") / -0.56f;
                            break;

                        case "node":
                            long nodeID = getLong(reader, "id");
                            float lon = getFloat(reader, "lon");
                            float lat = getFloat(reader, "lat");
                            idToNode.put(new Node(nodeID, lat, lon));
                            break;

                        case "way":
                            checkingRelation = false;
                            long wayID = getLong(reader, "id");
                            way = new Way(wayID);
                            isCoastline = false;
                            isBuilding = false;
                            break;

                        case "relation":
                            checkingRelation = true;
                            long relationID = getLong(reader, "id");
                            relation = new Relation(relationID);

                        case "member":
                            String type = reader.getAttributeValue(null, "type");
                            String memRef = reader.getAttributeValue(null, "ref");
                            if (type != null) {
                                if (type.equalsIgnoreCase("node")) {
                                    Node memNode = (Node) idToNode.get(Long.parseLong(memRef));
                                    if (memNode != null) {
                                        relation.addMember(memNode);
                                    }
                                } else if (type.equalsIgnoreCase("way")) {
                                    Way memWay = (Way) idToWay.get(Long.parseLong(memRef));
                                    if (memWay != null) {
                                        relation.addMember(memWay);
                                    }
                                } else if (type.equalsIgnoreCase("relation")) {
                                    Relation memRelation = (Relation) idToRelation.get(Long.parseLong(memRef));
                                    if (memRelation != null) {
                                        relation.addMember(memRelation);
                                    }
                                }
                            }

                        case "tag":
                            String k = reader.getAttributeValue(null, "k");
                            String v = reader.getAttributeValue(null, "v");
                            if (!checkingRelation) {
                                if (k.equals("natural") && v.equals("coastline")) {
                                    isCoastline = true;
                                } else if (k.equals("building")) {
                                    isBuilding = true;
                                } else {
                                    if (way != null) {
                                        if (extendedWay == null) {
                                            extendedWay = new ExtendedWay(way.getID());
                                        }
                                        extendedWay.addTag(k, v);
                                        extendedWay.setNodes(way.getNodes());
                                    }
                                }
                            }
                            break;

                        case "nd":
                            long ref = getLong(reader, "ref");
                            way.add((Node)idToNode.get(ref));
                            break;

                    }
                    break;
                case END_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "way":
                            idToWay.put(way);
                            if (isCoastline) {
                                coastlines.add(way);
                            }
                            if (isBuilding) {
                                buildings.add(way);
                            }
                            if (extendedWay != null) {
                                extendedWays.add(extendedWay);
                            }
                            break;
                        case "relation":
                            idToRelation.put(relation);
                            break;
                    }
                    break;
            }
        }
        islands = mergeCoastLines(coastlines);
        return new MapData(shapes, buildings, islands, extendedWays, minx, maxx, miny, maxy);
    }

    private List<Drawable> mergeCoastLines(List<Way> coastlines) {
        Map<Node, Way> pieces = new HashMap<>();

        for (Way coast : coastlines) {
            Way before = pieces.remove(coast.first());
            Way after = pieces.remove(coast.last());
            if (before == after) after = null;
            Way merged = Way.merge(before, coast, after);
            pieces.put(merged.first(), merged);
            pieces.put(merged.last(), merged);
        }
        List<Drawable> merged = new ArrayList<>();
        pieces.forEach((node, way) -> {
            if (way.last() == node) {
                merged.add(way);
            }
        });
        return merged;
    }

    private float getFloat(XMLStreamReader reader, String localName) {
        return Float.parseFloat(reader.getAttributeValue(null, localName));
    }

    private long getLong(XMLStreamReader reader, String localName) {
        return Long.parseLong(reader.getAttributeValue(null, localName));
    }
}
