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
package recorder.gui.script;
import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import recorder.result.Statement;
import recorder.result.StatementList;
/**
 * Represents a test script using a JTree.
 */
public class ScriptGui extends JTree {
    private StatementTreeModel myModel;


    public ScriptGui() {
        myModel = new StatementTreeModel(new StatementList());
        setModel(myModel);
        setName("script.tree.display");
        setRootVisible(false);
    }


    @Override
    public String convertValueToText(Object value, boolean selected, boolean expanded,
                                     boolean leaf, int row, boolean hasFocus) {
        if (value instanceof StatementList) {
            return "group";
        }
        if (value instanceof Statement) {
            System.identityHashCode(value);
            return ((Statement)value).toXml();
        }
        return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
    }


    public void display(StatementList statementList) {
        myModel.setRoot(statementList);
        if (statementList.lastResult() != null) {
            this.scrollPathToVisible(new TreePath(
                  new Object[]{statementList, statementList.lastResult()}));
        }
    }


    /**
     * A TreeModel for a StatementList.
     */
    private static class StatementTreeModel implements TreeModel {
        private StatementList root;
        private EventListenerList listenerList = new EventListenerList();


        StatementTreeModel(StatementList root) {
            this.root = root;
        }


        public Object getRoot() {
            return root;
        }


        public void setRoot(StatementList root) {
            this.root = root;
            fireTreeStructureChanged(root);
        }


        public Object getChild(Object parent, int index) {
            return ((StatementList)parent).get(index);
        }


        public int getChildCount(Object parent) {
            return ((StatementList)parent).size();
        }


        public boolean isLeaf(Object node) {
            return !(node instanceof StatementList);
        }


        public void valueForPathChanged(TreePath path, Object newValue) {
            throw new IllegalStateException("Impossible");
        }


        public int getIndexOfChild(Object parent, Object child) {
            return findIndex(parent, child);
        }


        private int findIndex(Object parent, Object child) {
            StatementList parentList = (StatementList)parent;
            for (int i = 0; i < parentList.size(); i++) {
                if (parentList.get(i) == child) {
                    return i;
                }
            }
            return -1;
        }


        public void addTreeModelListener(TreeModelListener listener) {
            listenerList.add(TreeModelListener.class, listener);
        }


        public void removeTreeModelListener(TreeModelListener listener) {
            listenerList.remove(TreeModelListener.class, listener);
        }


        protected void fireTreeStructureChanged(Statement oldRoot) {
            Object[] listeners = listenerList.getListenerList();
            TreeModelEvent event = new TreeModelEvent(this, new Object[]{oldRoot});

            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                ((TreeModelListener)listeners[i + 1]).treeStructureChanged(event);
            }
        }
    }
}
