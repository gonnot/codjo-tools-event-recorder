/*
 * codjo (Prototype)
 * =================
 *
 *    Copyright (C) 2005, 2012 by codjo.net
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *    implied. See the License for the specific language governing permissions
 *    and limitations under the License.
 */
package recorder.gesture;
import java.awt.*;
import javax.swing.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import recorder.component.GuiComponentFactory;
import recorder.event.GuiEvent;
import recorder.event.GuiEventList;
import recorder.event.GuiEventType;
import recorder.result.StatementList;
/**

 */
public class CloseFrameTest {
    private CloseFrame closeGesture;
    private String accessibleCloseName;
    private StatementList statementList;
    private GuiEventList eventList;


    @Test
    public void test_simple() throws Exception {
        JInternalFrame frame = new JInternalFrame("my internal frame");
        JButton closeButton = findCloseButton(frame);
        Assert.assertNotNull("JInternalFrame's close button should have been found", closeButton);

        eventList.addEvent(newClickGuiEvent(GuiEventType.BUTTON_CLICK, closeButton));

        closeGesture.receive(eventList, statementList);

        Assert.assertEquals("Click consumed", 0, eventList.size());

        Assert.assertEquals("<closeFrame title=\"my internal frame\"/>", statementList.toXml());
    }


    @Test
    public void test_simple_notCloseButton() throws Exception {
        JInternalFrame frame = new JInternalFrame("my internal frame");
        JButton aButton = new JButton();
        aButton.getAccessibleContext().setAccessibleName("bobo");
        frame.getContentPane().add(aButton);

        eventList.addEvent(newClickGuiEvent(GuiEventType.BUTTON_CLICK, aButton));

        closeGesture.receive(eventList, statementList);

        Assert.assertEquals("Click n'est pas consomm�", 1, eventList.size());
    }


    @Test
    public void test_simple_notInInternalFrame() throws Exception {
        JButton closeButton = new JButton();
        closeButton.getAccessibleContext().setAccessibleName(accessibleCloseName);

        eventList.addEvent(newClickGuiEvent(GuiEventType.BUTTON_CLICK, closeButton));

        closeGesture.receive(eventList, statementList);

        Assert.assertEquals("Click n'est pas consomm�", 1, eventList.size());
    }


    @Before
    public void setUp() throws Exception {
        closeGesture = new CloseFrame();
        this.accessibleCloseName = UIManager.getString("InternalFrameTitlePane.closeButtonAccessibleName");
        statementList = new StatementList();
        eventList = new GuiEventList();
    }


    private GuiEvent newClickGuiEvent(GuiEventType type, JButton closeButton) {
        return new GuiEvent(type, GuiComponentFactory.newGuiComponent(closeButton));
    }


    private JButton findCloseButton(Container frame) {
        if (isCloseButton(frame)) {
            return (JButton)frame;
        }

        Component[] content = frame.getComponents();
        for (Component aContent : content) {
            if (aContent instanceof Container) {
                Container container = (Container)aContent;
                JButton close = findCloseButton(container);
                if (close != null) {
                    return close;
                }
            }
        }

        return null;
    }


    private boolean isCloseButton(Component frame) {
        return frame.getAccessibleContext() != null
               && accessibleCloseName.equals(frame.getAccessibleContext().getAccessibleName());
    }
}
