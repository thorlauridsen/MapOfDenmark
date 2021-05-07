package bfst21.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bfst21.models.Model;
import bfst21.models.Util;
import bfst21.osm.ElementGroup;
import bfst21.osm.ElementSize;
import bfst21.osm.ElementType;
import bfst21.osm.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;


public class XmlParserTest {

    private Model model;

    @BeforeEach
    void setUp() throws XMLStreamException, IOException, ClassNotFoundException {
        model = new Model("data/amager.zip", false);
        model.load(true);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void getBuildingsSize_correctAmount() {
        int actual = model.getMapData().getWays(ElementGroup.getElementGroup(ElementType.BUILDING, ElementSize.DEFAULT)).size();
        assertEquals(3452081, actual);
        //buildings where k="building"
    }

    @Test
    public void getRelationsSize_correctAmount() {
        int actual = model.getMapData().getRelations().size();
        assertEquals(31727, actual);
    }

    @Test
    public void getCorrectDistanceBetween2Coordinates() {
        double distance = Util.distTo(12.6224313f, 55.6571112f,12.6238016f, 55.6573865f);
        assertEquals(0.09125, distance, 0.0001);
    }

    @Test
    public void getCorrectDistanceBetween2Coordinates2() {
        double lon1 = 12.485718754160853;
        double lat1 = 55.71871866715029;
        double lon2 = 11.559933323788288;
        double lat2 = 55.58933736433195;

        lat1 = -lat1 / 0.56f;
        lat2 = -lat2 / 0.56f;

        double distance = Util.distTo(lon1, lat1, lon2, lat2);
        assertEquals(59.83386186513035, distance, 0.1);
    }
}
