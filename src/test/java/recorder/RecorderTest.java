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
package recorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import recorder.component.GuiComponent;
import recorder.component.GuiComponentFactory;
import recorder.event.GuiEvent;
import recorder.event.GuiEventList;
import recorder.event.GuiEventType;
import recorder.result.AttributeList;
import recorder.result.DefaultStatement;
import recorder.result.Statement;
import recorder.result.StatementList;
/**

 */
public class RecorderTest {
    private Recorder recorder;


    @Test
    public void test_simplifiedEvent_setValue_combo() {
        JComboBox field = new JComboBox(new String[]{"la", "lb"});
        field.setName("myfield");
        field.setSelectedItem("la");

        recorder.eventDispatched(newFocus(field, FocusEvent.FOCUS_GAINED));
        recorder.eventDispatched(newFocus(new JTextField(), FocusEvent.FOCUS_GAINED));

        field.setSelectedItem("lb");

        recorder.eventDispatched(newFocus(field, FocusEvent.FOCUS_LOST));

        GuiEventList list = recorder.getSimpleEventList();
        Assert.assertEquals("Les events sont consomm�s imm�diatement", 0, list.size());

        Assert.assertEquals(new GuiEvent(GuiEventType.COMBO_FOCUS_GAIN, toGui(field), "la"),
                            list.findPrevious(new GuiEvent(GuiEventType.COMBO_FOCUS_GAIN, null)));
        Assert.assertEquals(new GuiEvent(GuiEventType.COMBO_FOCUS_LOST, toGui(field), "lb"),
                            list.findPrevious(new GuiEvent(GuiEventType.COMBO_FOCUS_LOST, null)));
    }


    @Test
    public void test_menu() {
        JMenu file = new JMenu("File");
        JMenuItem open = new JMenuItem("Open");
        file.add(open);
        recorder.eventDispatched(newMouse(file, MouseEvent.MOUSE_PRESSED));
        recorder.eventDispatched(newMouse(open, MouseEvent.MOUSE_PRESSED));

        StatementList result = recorder.getGestureResultList();

        Assert.assertEquals("<click menu=\"File:Open\"/>", result.toXml());
    }


    @Test
    public void test_setValue() {
        JTextField field = buildValidTextField();
        recorder.eventDispatched(newKey(field, KeyEvent.KEY_RELEASED, 'e'));

        StatementList result = recorder.getGestureResultList();

        Assert.assertEquals("<setValue name=\"myfield\" value=\"e\"/>", result.toXml());
    }


    @Test
    public void test_removeLastGesture() {
        JTextField field = new JTextField();
        field.setName("myfield");
        JTextField fields2 = new JTextField();
        fields2.setName("myfield2");

        recorder.eventDispatched(newKey(field, KeyEvent.KEY_RELEASED, ' '));
        recorder.eventDispatched(newKey(fields2, KeyEvent.KEY_RELEASED, 'e'));

        recorder.removeLastGesture();
        StatementList result = recorder.getGestureResultList();

        Assert.assertEquals("<setValue name=\"myfield\" value=\"\"/>", result.toXml());
    }


    @Test
    public void test_postGestureResult() {
        MockRecorderListener listener = new MockRecorderListener();
        recorder.addRecorderListener(listener);

        Statement statement = new DefaultStatement("assertion", AttributeList.EMPTY_LIST);
        recorder.postGestureResult(statement);

        Assert.assertEquals("Le listener est prevenu", 1, listener.eventRecognizedCalled);
        Assert.assertSame("Le result est ajout�", statement,
                          recorder.getGestureResultList().lastResult());
    }


    @Test
    public void test_recorderListener_2listeners() {
        MockRecorderListener listener = new MockRecorderListener();
        MockRecorderListener listener2 = new MockRecorderListener();
        recorder.addRecorderListener(listener);
        recorder.addRecorderListener(listener2);

        recorder.eventDispatched(newKey(buildValidTextField(), KeyEvent.KEY_RELEASED, 'e'));

        Assert.assertEquals("Event released est reconnu : listener1", 1,
                            listener.eventRecognizedCalled);
        Assert.assertEquals("Event released est reconnu : listener2", 1,
                            listener2.eventRecognizedCalled);

        recorder.removeRecorderListener(listener2);
        recorder.eventDispatched(newKey(buildValidTextField(), KeyEvent.KEY_RELEASED, 'e'));

        Assert.assertEquals("Event released est reconnu : listener1", 2,
                            listener.eventRecognizedCalled);
        Assert.assertEquals("Event released est reconnu : listener2", 1,
                            listener2.eventRecognizedCalled);
    }


    @Test
    public void test_recorderListener_casParticulier() {
        MockRecorderListener listener = new MockRecorderListener();

        // Enlever un listener ne fait pas de NPA :)
        recorder.removeRecorderListener(listener);

        // Un clear fait un event
        recorder.addRecorderListener(listener);
        recorder.clearScript();
        Assert.assertEquals("Un clear fait un event", 1, listener.eventRecognizedCalled);

        // Un awtEvent non reconnu ne fait pas d'event
        recorder.eventDispatched(newKey(buildValidTextField(), KeyEvent.KEY_PRESSED, 'e'));
        Assert.assertEquals("Non reconnu pas d'event", 1, listener.eventRecognizedCalled);

        // Un awtEvent reconnu fait un event
        recorder.eventDispatched(newKey(buildValidTextField(), KeyEvent.KEY_RELEASED, 'e'));
        Assert.assertEquals("Un awtEvent reconnu declenche", 2, listener.eventRecognizedCalled);

        // Un removeLastGesture fait un event
        recorder.removeLastGesture();
        Assert.assertEquals("Un remove fait un event", 3, listener.eventRecognizedCalled);
    }


    @Before
    public void setUp() throws Exception {
        recorder = new Recorder(new GuiComponentFactory());
    }


    private JTextField buildValidTextField() {
        JTextField field = new JTextField();
        field.setName("myfield");
        field.setText("e");
        return field;
    }


    private MouseEvent newMouse(Component source, int id) {
        return new MouseEvent(source, id, 0, MouseEvent.BUTTON1_MASK, 0, 0, 0, false);
    }


    private KeyEvent newKey(Component source, int id, char ch) {
        return new KeyEvent(source, id, 0, 0, 0, ch);
    }


    private FocusEvent newFocus(Component source, int id) {
        return new FocusEvent(source, id);
    }


    private GuiComponent toGui(JComponent field) {
        return GuiComponentFactory.newGuiComponent(field);
    }


    private static class MockRecorderListener implements RecorderListener {
        int eventRecognizedCalled = 0;


        public void recorderUpdate() {
            eventRecognizedCalled++;
        }
    }
}
