package ColorioServer;

import ColorioCommon.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static ColorioCommon.Constants.startingWeight;


public class GameTest {

    /**
     * Test for the expected Player functionality
     */
    @Test
    public void PlayerTest(){
        Color c = new Color(0.7f,0.7f,0.7f);
        Player p = new Player(new Centroid(50.0, 45.0, 10, c),
                                new Centroid(50.0, 55.0, 10, c),
                                new Centroid(45, 50, 10, c),
                                new Centroid(55, 50, 10, c));
        Assertions.assertEquals(50.0, p.calculateMiddleX());
        Assertions.assertEquals(50.0, p.calculateMiddleY());
    }


}
