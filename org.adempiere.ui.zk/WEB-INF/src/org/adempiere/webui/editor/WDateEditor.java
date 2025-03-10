/******************************************************************************
 * Product: Posterita Ajax UI 												  *
 * Copyright (C) 2007 Posterita Ltd.  All Rights Reserved.                    *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * Posterita Ltd., 3, Draper Avenue, Quatre Bornes, Mauritius                 *
 * or via info@posterita.org or http://www.posterita.org/                     *
 *****************************************************************************/

package org.adempiere.webui.editor;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

import org.adempiere.webui.ValuePreference;
import org.adempiere.webui.component.Datebox;
import org.adempiere.webui.event.ContextMenuEvent;
import org.adempiere.webui.event.ContextMenuListener;
import org.adempiere.webui.event.ValueChangeEvent;
import org.adempiere.webui.window.WFieldRecordInfo;
import org.compiere.model.GridField;
import org.compiere.util.CLogger;
import org.compiere.util.DisplayType;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;

/**
 * Default editor for {@link DisplayType#Date}.<br/>
 * Implemented with {@link Datebox} component.
 * @author <a href="mailto:agramdass@gmail.com">Ashley G Ramdass</a>
 * @date Mar 12, 2007
 */
public class WDateEditor extends WEditor implements ContextMenuListener
{
	private static final String[] LISTENER_EVENTS = {Events.ON_CHANGE, Events.ON_OK};
    @SuppressWarnings("unused")
	private static final CLogger logger;

    static
    {
        logger = CLogger.getCLogger(WDateEditor.class);
    }

    private Timestamp oldValue = new Timestamp(0);
    
    /**
    *
    * @param gridField
    */
    public WDateEditor(GridField gridField)
    {
    	this(gridField, false, null);
    }
    
    /**
     * 
     * @param gridField
     * @param tableEditor
     * @param editorConfiguration
     */
    public WDateEditor(GridField gridField, boolean tableEditor, IEditorConfiguration editorConfiguration)
    {
        super(new Datebox(), gridField, tableEditor, editorConfiguration);
        init();
    }

	/**
	 * Constructor for use if a grid field is unavailable
	 *
	 * @param label
	 *            field label
	 * @param description
	 *            field description
	 * @param mandatory
	 *            whether field is mandatory
	 * @param readonly
	 *            whether or not the editor is read only
	 * @param updateable
	 *            whether the editor contents can be changed
	 */
	public WDateEditor (String label, String description, boolean mandatory, boolean readonly, boolean updateable)
	{
		super(new Datebox(), label, description, mandatory, readonly, updateable);
		setColumnName("Date");
		init();
	}

	/**
	 * Default constructor
	 */
	public WDateEditor()
	{
		this("Date", "Date", false, false, true);
	}   // VDate

	/**
	 *
	 * @param columnName
	 * @param mandatory
	 * @param readonly
	 * @param updateable
	 * @param title
	 */
	public WDateEditor(String columnName, boolean mandatory, boolean readonly, boolean updateable,
			String title)
	{
		super(new Datebox(), columnName, title, null, mandatory, readonly, updateable);
	}

	/**
	 * Init component and context menu
	 */
	private void init()
	{
		popupMenu = new WEditorPopupMenu(false, false, isShowPreference());
		popupMenu.addMenuListener(this);
		addChangeLogMenu(popupMenu);
		if (gridField != null)
			getComponent().setPlaceholder(gridField.getPlaceholder());
	}

	@Override
	public void onEvent(Event event)
    {
		if (Events.ON_CHANGE.equalsIgnoreCase(event.getName()) || Events.ON_OK.equalsIgnoreCase(event.getName()))
		{
	        Date date = getComponent().getValue();
	        Timestamp newValue = null;

	        if (date != null)
	        {
	        	newValue = Timestamp.valueOf(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
	        }
	        if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
	    	    return;
	    	}
	        if (oldValue == null && newValue == null) {
	        	return;
	        }
	        ValueChangeEvent changeEvent = new ValueChangeEvent(this, this.getColumnName(), oldValue, newValue);
	        super.fireValueChange(changeEvent);
	        oldValue = newValue;
		}
    }

    @Override
    public String getDisplay()
    {
    	return getComponent().getText();
    }

    @Override
    public Timestamp getValue()
    {
    	if(getComponent().getValue() == null) return null;
    	return Timestamp.valueOf(getComponent().getValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    @Override
    public void setValue(Object value)
    {
    	if (value == null || value.toString().trim().length() == 0)
    	{
    		Timestamp currentValue = oldValue;
    		oldValue = null;
    		getComponent().setValue(null);
    		if (currentValue != null && !readOnly)
    		{
    			ValueChangeEvent changeEvent = new ValueChangeEvent(this, this.getColumnName(), currentValue, null);
    			super.fireValueChange(changeEvent);
    		}
    	}
    	else if (value instanceof Timestamp)
        {
    		Timestamp currentValue = oldValue;
    		LocalDateTime localDateTime = ((Timestamp)value).toLocalDateTime();
            getComponent().setValueInLocalDateTime(localDateTime);            
            oldValue = Timestamp.valueOf(localDateTime);
			if (!Objects.equals(currentValue, oldValue) && !readOnly) 
            {
            	ValueChangeEvent changeEvent = new ValueChangeEvent(this, this.getColumnName(), currentValue, oldValue);
            	super.fireValueChange(changeEvent);
            }
        }
    	else
    	{
    		try
    		{
    			Timestamp currentValue = oldValue;
    			getComponent().setText(value.toString());
    			if (getComponent().getValue() != null)
        			oldValue = Timestamp.valueOf(getComponent().getValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        		else
        			oldValue = null;
    			if (!Objects.equals(currentValue, oldValue) && !readOnly)
    			{
	    			ValueChangeEvent changeEvent = new ValueChangeEvent(this, this.getColumnName(), currentValue, oldValue);
	                super.fireValueChange(changeEvent);
    			}
    		} catch (Exception e) {}    		
    	}
    }

	@Override
	public Datebox getComponent() {
		return (Datebox) component;
	}

	@Override
	public boolean isReadWrite() {
		return getComponent().isEnabled();
	}


	@Override
	public void setReadWrite(boolean readWrite) {
		getComponent().setEnabled(readWrite);
	}

	@Override
	public String[] getEvents()
    {
        return LISTENER_EVENTS;
    }

	@Override
	public void onMenu(ContextMenuEvent evt) {
		if (WEditorPopupMenu.CHANGE_LOG_EVENT.equals(evt.getContextEvent()))
		{
			WFieldRecordInfo.start(gridField);
		}
		else if (WEditorPopupMenu.PREFERENCE_EVENT.equals(evt.getContextEvent()) && gridField != null)
		{
			if (isShowPreference())
				ValuePreference.start (getComponent(), this.getGridField(), getValue());
			return;
		}
	}

}
