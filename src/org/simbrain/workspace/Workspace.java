/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2005,2007 The Authors.  See http://www.simbrain.net/credits
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.simbrain.workspace;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

/**
 * A collection of components which interact via couplings.   Neural networks, datatables, gauges, and scripts are examples of components in a Simbrain workspace.
 * Essentially, an instance of a workspace corresponds to a single simulation (though at some point it will be possible to link multiple workspaces on different machines together).
 * 
 * A workspace can be visualized via a {@link org.simbrain.workspace.gui.SimbrainDesktop}.
 * 
 * @see org.simbrain.workspace.Coupling
 *
 */
public class Workspace {
    /** The time to sleep between updates. */
    private static final int SLEEP_INTERVAL = 1;
    
    /** The default serial version ID. */
    private static final long serialVersionUID = 1L;

    /** The static logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(Workspace.class);
    
    /** The coupling manager for this workspace. */
    private final CouplingManager manager = new CouplingManager();
    
    /** List of workspace components. */
    private List<WorkspaceComponent<?>> componentList = Collections.synchronizedList(new ArrayList<WorkspaceComponent<?>>());

    /** Sentinel for determining if workspace has been changed since last save. */
    private boolean workspaceChanged = false;
    
    /** Current workspace file. */
    private File currentFile = new File(WorkspacePreferences.getDefaultFile());

    /** Current directory. So when re-opening this type of component the app remembers where to look. */
    private String currentDirectory = WorkspacePreferences.getCurrentDirectory();

    /** Thread which runs workspace. */
    private WorkspaceThread workspaceThread;

    /** Listeners on this workspace. */
    private Set<WorkspaceListener> listeners = new HashSet<WorkspaceListener>();

    /**
     * Mapping from workspace component types to integers which show how many have been added.
     * For naming.
     */
    private Hashtable<Class<?>, Integer> componentNameIndices = new Hashtable<Class<?>, Integer>();

    /**
     *  Enumeration for the update methods
     *  BUFFER: default update method; based on buffering
     *  PRIORITYBASED: user sets the priority for each component and the coupling manager
     *  Elements with smaller priority number (i.e., higher priority)
     *   are updated first.
     */
    public static enum UpdateMethod { BUFFERED, PRIORITY_BASED, CUSTOM}

    /** Current update method. */
    private UpdateMethod updateMethod = UpdateMethod.BUFFERED;
    
    /** Custom update method; null if none. */
    private CustomUpdate customUpdateMethod = null;
    
    /**
     * Used in prioirty based update.
     */
    private SortedSet<Integer> updatePriorities =  new TreeSet<Integer>();
    
    // TODO: Add docs
    private boolean fireEvents = true;

    
    /**
     * Adds a listener to the workspace.
     * 
     * @param listener the Listener to add.
     */
    public void addListener(final WorkspaceListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes the listener from the workspace.
     * 
     * @param listener The listener to remove.
     */
    public void removeListener(final WorkspaceListener listener) {
        listeners.remove(listener);
    }
    
    void toggleEvents(final boolean on) {
        this.fireEvents = on;
    }

    /**
     * Couple each source attribute to all target attributes.
     * 
     * @param sourceAttributes source producing attributes
     * @param targetAttributes target consuming attributes
     */
    public void coupleOneToMany(ArrayList<ProducingAttribute<?>> sourceAttributes, ArrayList<ConsumingAttribute<?>> targetAttributes) {
        for (ProducingAttribute<?> producingAttribute : sourceAttributes) {
            for (ConsumingAttribute<?> consumingAttribute : targetAttributes) {
                Coupling<?> coupling = new Coupling(producingAttribute, consumingAttribute);
                getCouplingManager().addCoupling(coupling);
            }
        }
    }
    
    /**
     * Couple each source attribute to one target attribute, as long as there are target attributes
     * to couple to.
     * 
     * @param sourceAttributes source producing attributes
     * @param targetAttributes target producing attributes
     */
    public void coupleOneToOne(ArrayList<ProducingAttribute<?>> sourceAttributes, ArrayList<ConsumingAttribute<?>> targetAttributes) {
        Iterator<ConsumingAttribute<?>> consumingAttributes = targetAttributes.iterator();
        for (ProducingAttribute<?> producingAttribute : sourceAttributes) {
            if (consumingAttributes.hasNext()) {
                Coupling<?> coupling = new Coupling(producingAttribute, consumingAttributes.next());
                getCouplingManager().addCoupling(coupling);
            }
        }
    }

    /**
     * Adds a workspace component to the workspace.
     * 
     * @param component The component to add.
     */
    public void addWorkspaceComponent(final WorkspaceComponent<?> component) {
        LOGGER.debug("adding component: " + component);
        componentList.add(component);
        component.setWorkspace(this);
        workspaceChanged = true;
        
        /*
         * Handle component naming.
         * 
         * If the component has not yet been named, name as follows:
         *      (ClassName - "Component") + index
         * where index iterates as new components are added.
         * e.g. Network 1, Network 2, etc.
         */
        if (component.getName().equalsIgnoreCase("")) {
            if (componentNameIndices.get(component.getClass()) == null) {
                componentNameIndices.put(component.getClass(), 1);
            } else {
                int index = componentNameIndices.get(component.getClass());
                componentNameIndices.put(component.getClass(), index + 1);
            }
            component.setName(component.getSimpleName() + 
                    componentNameIndices.get(component.getClass()));
        }
 
        // Notify listeners 
        if (fireEvents) {
            for (WorkspaceListener listener : listeners) {
                listener.componentAdded(component);
            }
        }

    }
    
    /**
     * Adds a workspace component to the workspace with a priority.
     * 
     * @param component The component to add.
     */
    public void addWorkspaceComponent(final WorkspaceComponent<?> component, int priority) {
        updatePriorities.add(new Integer(priority));
        addWorkspaceComponent(component);
    }
    
    /**
     * Remove the specified component.
     *
     * @param component The component to remove.
     */
    public void removeWorkspaceComponent(final WorkspaceComponent<?> component) {
        LOGGER.debug("removing component: " + component);
        for (WorkspaceListener listener : listeners) {
            listener.componentRemoved(component);
        }
        this.getCouplingManager().removeCouplings(component);   // Remove all couplings assciated with this component
        componentList.remove(component);
        this.setWorkspaceChanged(true);
    }
    
    // TODO: Add other update methods.
    
    /**
     * Update all couplings on all components.  Currently use a buffering method.
     */
    public void globalUpdate() {

        if (updateMethod == UpdateMethod.BUFFERED) {
                manager.updateAllCouplings();
                synchronized (componentList) {
                for (WorkspaceComponent<?> component : componentList) {
                    component.doUpdate();
                    //System.out.println(component + ": " + (System.nanoTime() - time));
                    try {
                        Thread.sleep(SLEEP_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (updateMethod == UpdateMethod.PRIORITY_BASED) {
            for (Integer i : updatePriorities) {
                // TODO!
            }

        } else if (updateMethod == UpdateMethod.CUSTOM) {
               if (customUpdateMethod == null) {
                   // TODO: Throw Exception
               } else {
                   synchronized (componentList) {
                       customUpdateMethod.update(this);
                   }

               }
        }
        
        time++;
    }
    
    /**
     * Should be called when updating is stopped.
     */
    void updateStopped() {
        synchronized (componentList) {
            for (WorkspaceComponent<?> component : componentList) {
                component.doStopped();
            }
        }
    }
    
    /**
     * Update all couplings on all components once.
     */
    public void singleUpdate() {
        globalUpdate();
        updateStopped();
    }
    
    /**
     * Adds a coupling to the CouplingManager.
     * 
     * @param coupling The coupling to add.
     */
    public void addCoupling(final Coupling<?> coupling) {
        manager.addCoupling(coupling);
    }
    
    /**
     * Removes a coupling from the CouplingManager.
     * 
     * @param coupling The coupling to remove.
     */
    public void removeCoupling(final Coupling<?> coupling) {
        manager.removeCoupling(coupling);
    }

    
    /**
     * Iterates all couplings on all components until halted by user.
     */
    public void globalRun() {
        WorkspaceThread workspaceThread = getWorkspaceThread();

        if (!workspaceThread.isRunning()) {
            workspaceThread.setRunning(true);
            workspaceThread.start();
        } else {
            workspaceThread.setRunning(false);
        }
    }

    /**
     * Stops iteration of all couplings on all components.
     */
    public void globalStop() {
        WorkspaceThread workspaceThread = getWorkspaceThread();

        workspaceThread.setRunning(false);
        clearWorkspaceThread();
    }

    /**
     * Remove all components (networks, worlds, etc.) from this workspace.
     */
    public void clearWorkspace() {
        removeAllComponents();
        workspaceChanged = false;
        currentFile = null;
        for (WorkspaceListener listener : listeners) {
            listener.workspaceCleared();
        }
        manager.clearCouplings();
    }

    /**
     * Disposes all Simbrain Windows.
     */
    public void removeAllComponents() {
        ArrayList<WorkspaceComponent> toRemove = new ArrayList();
        synchronized (componentList) {
            for (WorkspaceComponent<?> component : componentList) {
                toRemove.add(component);
            }
            for (WorkspaceComponent<?> component : toRemove) {
                removeWorkspaceComponent(component);
            }
        }
    }

    /**
     * Check whether there have been changes in the workspace or its components.
     *
     * @return true if changes exist, false otherwise
     */
    public boolean changesExist() {
        if (workspaceChanged) {
            return true;
        } else {
            boolean hasChanged = false;
            synchronized (componentList) {
                for (WorkspaceComponent<?> window : componentList) {
                    if (window.hasChangedSinceLastSave()) {
                        hasChanged = true;
                    }
                }
            }
            return hasChanged;
        }
    }

    /**
     * Sets whether the workspace has been changed.
     * @param workspaceChanged Has workspace been changed value
     */
    public void setWorkspaceChanged(final boolean workspaceChanged) {
        this.workspaceChanged = workspaceChanged;
    }

    /**
     * @return the currentDirectory
     */
    public String getCurrentDirectory() {
        return currentDirectory;
    }

    /**
     * @param currentDirectory the currentDirectory to set
     */
    public void setCurrentDirectory(final String currentDirectory) {
        this.currentDirectory = currentDirectory;
        WorkspacePreferences.setCurrentDirectory(currentDirectory);
    }

    /**
     * @return Returns the currentFile.
     */
    public File getCurrentFile() {
        return currentFile;
    }

    /**
     * @param currentFile The current_file to set.
     */
    public void setCurrentFile(final File currentFile) {
        this.currentFile = currentFile;
        WorkspacePreferences.setDefaultFile(currentFile.getAbsolutePath());
    }

    /**
     * @return the componentList
     */
    public List<? extends WorkspaceComponent<?>> getComponentList() {
        return Collections.unmodifiableList(componentList);
    }
    
    /**
     * Get a component using its name id.  Used in terminal mode.
     *
     * @param id name of component
     * @return Workspace Component
     */
    public WorkspaceComponent<?> getComponent(final String id) {
        synchronized (componentList) {
            for (WorkspaceComponent<?> component : componentList) {
                if (component.getName().equalsIgnoreCase(id)) {
                    return component;
                }
            }
        }
        return null;
    }
    
    /**
     * Returns the workspaceThread.
     * 
     * @return The workspaceThread.
     */
    public WorkspaceThread getWorkspaceThread() {
        if (workspaceThread == null) { workspaceThread = new WorkspaceThread(this); }
        
        return workspaceThread;
    }
    
    /**
     * Clears the workspace thread.
     */
    private void clearWorkspaceThread() {
        workspaceThread = null;
    }

    /**
     * Returns the coupling manager for this workspace.
     * 
     * @return The coupling manager for this workspace.
     */
    public CouplingManager getCouplingManager() {
        return manager;
    }
  
    /**
     * {@inheritDoc}
     */
    public String toString() {
        StringBuilder builder = new StringBuilder("Number of components: " + componentList.size() + "\n");
        int i = 0;
        synchronized (componentList) {
            for (WorkspaceComponent<?> component : componentList) {
                builder.append("Component " + ((i++)+1) + ":" + component.getName() + "\n");
            }
        }
        return builder.toString();
    }

    public UpdateMethod getUpdateMethod() {
        return updateMethod;
    }

    public void setUpdateMethod(UpdateMethod updateMethod) {
        this.updateMethod = updateMethod;
    }

    public CustomUpdate getCustomUpdateMethod() {
        return customUpdateMethod;
    }

    public void setCustomUpdateMethod(CustomUpdate customUpdateMethod) {
        this.customUpdateMethod = customUpdateMethod;
    }

    /** Provisional global time implementation. */
    private int time = 0;
    
    public Number getTime() {
        return time;
    }
    
}
