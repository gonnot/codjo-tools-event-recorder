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
package recorder.gui.action;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
public class GuiActionManager {
    private Map<Object, GuiAction> storage = new HashMap<Object, GuiAction>();


    public void declare(GuiAction action) {
        storage.put(action.getValue(GuiAction.ACTION_ID), action);
    }


    public GuiAction getAction(String actionId) {
        return storage.get(actionId);
    }


    public Iterator<GuiAction> actions() {
        return storage.values().iterator();
    }
}
