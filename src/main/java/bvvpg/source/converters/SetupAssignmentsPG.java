/*
 * #%L
 * Volume rendering of bdv datasets with gamma and transparency option
 * %%
 * Copyright (C) 2022 - 2025 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package bvvpg.source.converters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.imglib2.type.numeric.ARGBType;

import org.jdom2.Element;

import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.SetupAssignments;
import mpicbg.spim.data.XmlHelpers;

/**
 * Manage a (fixed) set of {@link ConverterSetup}s and (changing) set of
 * {@link MinMaxGroupPG}s, such that the following is always true:
 * <ol>
 * <li>Every setup is assigned to exactly one group.</li>
 * <li>No group is empty.</li>
 * </ol>
 *
 * @author Tobias Pietzsch
 */
@Deprecated
public class SetupAssignmentsPG
{
	/**
	 * All {@link ConverterSetup}s managed by this SetupAssignments.
	 */
	private final ArrayList< ConverterSetup > setups;

	/**
	 * A list of {@link MinMaxGroupPG}s, such that every {@link #setups setup} is
	 * contained in exactly one group.
	 */
	private final ArrayList< MinMaxGroupPG > minMaxGroups;

	/**
	 * Maps every {@link #setups setup} to the {@link #minMaxGroups group}
	 * containing it.
	 */
	private final Map< ConverterSetup, MinMaxGroupPG > setupToGroup;

	private final double fullRangeMin;

	private final double fullRangeMax;

	private final double defaultRangeMin;

	private final double defaultRangeMax;

	private static final double minIntervalSize = 0;

	public interface UpdateListener
	{
		void update();
	}

	private UpdateListener updateListener;

	public SetupAssignmentsPG( final ArrayList< ConverterSetup > converterSetups, final double defaultRangeMin, final double defaultRangeMax )
	{
		this( converterSetups, Integer.MIN_VALUE, Integer.MAX_VALUE, defaultRangeMin, defaultRangeMax );
	}

	/**
	 *
	 * @param converterSetups
	 * @param fullRangeMin
	 * @param fullRangeMax
	 * @param defaultRangeMin
	 * @param defaultRangeMax
	 */
	public SetupAssignmentsPG( final ArrayList< ConverterSetup > converterSetups, final double fullRangeMin, final double fullRangeMax, final double defaultRangeMin, final double defaultRangeMax )
	{
		setups = new ArrayList<>( converterSetups );
		minMaxGroups = new ArrayList<>();
		setupToGroup = new HashMap<>();
		this.fullRangeMin = fullRangeMin;
		this.fullRangeMax = fullRangeMax;
		this.defaultRangeMin = defaultRangeMin;
		this.defaultRangeMax = defaultRangeMax;
		for ( final ConverterSetup setup : setups )
		{
			final double displayRangeMin = Math.max( defaultRangeMin, Math.min( defaultRangeMax, setup.getDisplayRangeMin() ) );
			final double displayRangeMax = Math.max( defaultRangeMin, Math.min( defaultRangeMax, setup.getDisplayRangeMax() ) );
			if ( setup.getDisplayRangeMin() != displayRangeMin || setup.getDisplayRangeMax() != displayRangeMax )
				setup.setDisplayRange( displayRangeMin, displayRangeMax );
			final MinMaxGroupPG group = new MinMaxGroupPG( fullRangeMin, fullRangeMax, defaultRangeMin, defaultRangeMax, displayRangeMin, displayRangeMax, minIntervalSize );
			minMaxGroups.add( group );
			setupToGroup.put( setup, group );
			group.addSetup( setup );
		}
		updateListener = null;
	}

	/**
	 * Add the specified setup to the specified group. The setup is removed from
	 * its previous group. If this previous group is made empty by this, it is
	 * removed from the list of groups.
	 */
	public void moveSetupToGroup( final ConverterSetup setup, final MinMaxGroupPG group )
	{
		final MinMaxGroupPG oldGroup = setupToGroup.get( setup );
		if ( oldGroup == group )
			return;

		setupToGroup.put( setup, group );
		group.addSetup( setup );

		final boolean oldGroupIsEmpty = oldGroup.removeSetup( setup );
		if ( oldGroupIsEmpty )
			minMaxGroups.remove( oldGroup );

		if ( updateListener != null )
			updateListener.update();
	}

	/**
	 * Remove the specified setup from the specified group. If this group would
	 * be made empty by this, it is not removed from the group. Otherwise, after
	 * being removed, a new group is created containing only the specified
	 * setup, and this new group is added to the list of group. The settings of
	 * the new group are initialized with the settings of the old group.
	 *
	 * @return Whether or not removal was successful (so the corresponding
	 *         checkbox can be re-checked)
	 */
	public boolean removeSetupFromGroup( final ConverterSetup setup, final MinMaxGroupPG group )
	{
		if ( setupToGroup.get( setup ) != group )
			return false;
		if ( group.setups.size() == 1 )
			return false;

		final MinMaxGroupPG newGroup = new MinMaxGroupPG( group.getFullRangeMin(), group.getFullRangeMax(), group.getRangeMin(), group.getRangeMax(), setup.getDisplayRangeMin(), setup.getDisplayRangeMax(), minIntervalSize );
		minMaxGroups.add( newGroup );
		setupToGroup.put( setup, newGroup );
		newGroup.addSetup( setup );

		final boolean groupIsEmpty = group.removeSetup( setup );
		if ( groupIsEmpty )
			minMaxGroups.remove( group );

		if ( updateListener != null )
			updateListener.update();
		return true;
	}

	public void setUpdateListener( final UpdateListener l )
	{
		updateListener = l;
	}

	/**
	 * @return the list of {@link MinMaxGroupPG}s, such that every {@link #setups
	 *         setup} is contained in exactly one group.
	 */
	public List< MinMaxGroupPG > getMinMaxGroups()
	{
		return Collections.unmodifiableList( minMaxGroups );
	}

	/**
	 * @return a list of all {@link ConverterSetup}s.
	 */
	public List< ConverterSetup > getConverterSetups()
	{
		return Collections.unmodifiableList( setups );
	}

	/**
	 * @return the {@link MinMaxGroupPG} that contains {@code setup}, currently.
	 */
	public MinMaxGroupPG getMinMaxGroup( final ConverterSetup setup )
	{
		return setupToGroup.get( setup );
	}

	/**
	 * Add the setup in a new group.
	 * @param setup
	 */
	public void addSetup( final ConverterSetup setup )
	{
		final MinMaxGroupPG group = new MinMaxGroupPG( fullRangeMin, fullRangeMax, defaultRangeMin, defaultRangeMax, setup.getDisplayRangeMin(), setup.getDisplayRangeMax(), minIntervalSize );
		minMaxGroups.add( group );
		setupToGroup.put( setup, group );
		group.addSetup( setup );
		setups.add( setup );
		if ( updateListener != null )
			updateListener.update();
	}

	public void removeSetup( final ConverterSetup setup )
	{
		final MinMaxGroupPG group = setupToGroup.get( setup );
		if ( group == null )
			return;
		final boolean groupIsEmpty = group.removeSetup( setup );
		if ( groupIsEmpty )
			minMaxGroups.remove( group );
		setups.remove( setup );
		setupToGroup.remove( setup );
		if ( updateListener != null )
			updateListener.update();
	}

	/**
	 * Serialize the state of this {@link SetupAssignments} to XML.
	 */
	public Element toXml()
	{
		final Element elem = new Element( "SetupAssignments" );

		final Element elemConverterSetups = new Element( "ConverterSetups" );
		for ( final ConverterSetup setup : setups )
		{
			final Element elemConverterSetup = new Element( "ConverterSetup" );
			elemConverterSetup.addContent( XmlHelpers.intElement( "id", setup.getSetupId() ) );
			elemConverterSetup.addContent( XmlHelpers.doubleElement( "min", setup.getDisplayRangeMin() ) );
			elemConverterSetup.addContent( XmlHelpers.doubleElement( "max", setup.getDisplayRangeMax() ) );
			elemConverterSetup.addContent( XmlHelpers.intElement( "color", setup.getColor().get() ) );
			elemConverterSetup.addContent( XmlHelpers.intElement( "groupId", minMaxGroups.indexOf( setupToGroup.get( setup ) ) ) );
			elemConverterSetups.addContent( elemConverterSetup );
		}
		elem.addContent( elemConverterSetups );

		final Element elemMinMaxGroups = new Element( "MinMaxGroups" );
		for ( int i = 0; i < minMaxGroups.size(); ++i )
		{
			final MinMaxGroupPG group = minMaxGroups.get( i );
			final Element elemMinMaxGroup = new Element( "MinMaxGroup" );
			elemMinMaxGroup.addContent( XmlHelpers.intElement( "id", i ) );
			elemMinMaxGroup.addContent( XmlHelpers.doubleElement( "fullRangeMin", group.getFullRangeMin() ) );
			elemMinMaxGroup.addContent( XmlHelpers.doubleElement( "fullRangeMax", group.getFullRangeMax() ) );
			elemMinMaxGroup.addContent( XmlHelpers.doubleElement( "rangeMin", group.getRangeMin() ) );
			elemMinMaxGroup.addContent( XmlHelpers.doubleElement( "rangeMax", group.getRangeMax() ) );
			elemMinMaxGroup.addContent( XmlHelpers.doubleElement( "currentMin", group.getMinBoundedValue().getCurrentValue() ) );
			elemMinMaxGroup.addContent( XmlHelpers.doubleElement( "currentMax", group.getMaxBoundedValue().getCurrentValue() ) );
			elemMinMaxGroups.addContent( elemMinMaxGroup );
		}
		elem.addContent( elemMinMaxGroups );

		return elem;
	}

	/**
	 * Restore the state of this {@link SetupAssignments} from XML. Note, that
	 * this only restores the assignments of setups to groups and group
	 * settings. The list of {@link ConverterSetup}s is not restored.
	 */
	public void restoreFromXml( final Element parent )
	{
		final Element elemSetupAssignments = parent.getChild( "SetupAssignments" );
		if ( elemSetupAssignments == null )
			return;
		final Element elemConverterSetups = elemSetupAssignments.getChild( "ConverterSetups" );
		final List< Element > converterSetupNodes = elemConverterSetups.getChildren( "ConverterSetup" );
		if ( converterSetupNodes.size() != setups.size() )
			throw new IllegalArgumentException();

		final Element elemMinMaxGroups = elemSetupAssignments.getChild( "MinMaxGroups" );
		final List< Element > minMaxGroupNodes = elemMinMaxGroups.getChildren( "MinMaxGroup" );
		minMaxGroups.clear();
		for ( int i = 0; i < minMaxGroupNodes.size(); ++i )
			minMaxGroups.add( null );
		for ( final Element elem : minMaxGroupNodes  )
		{
			final int id = Integer.parseInt( elem.getChildText( "id" ) );
			final double fullRangeMin1 = Double.parseDouble( elem.getChildText( "fullRangeMin" ) );
			final double fullRangeMax1 = Double.parseDouble( elem.getChildText( "fullRangeMax" ) );
			final double rangeMin = Double.parseDouble( elem.getChildText( "rangeMin" ) );
			final double rangeMax = Double.parseDouble( elem.getChildText( "rangeMax" ) );
			final double currentMin = Double.parseDouble( elem.getChildText( "currentMin" ) );
			final double currentMax = Double.parseDouble( elem.getChildText( "currentMax" ) );
			minMaxGroups.set( id, new MinMaxGroupPG( fullRangeMin1, fullRangeMax1, rangeMin, rangeMax, currentMin, currentMax, minIntervalSize ) );
		}

		for ( final Element elem : converterSetupNodes )
		{
			final int id = Integer.parseInt( elem.getChildText( "id" ) );
			final double min = Double.parseDouble( elem.getChildText( "min" ) );
			final double max = Double.parseDouble( elem.getChildText( "max" ) );
			final int color = Integer.parseInt( elem.getChildText( "color" ) );
			final int groupId = Integer.parseInt( elem.getChildText( "groupId" ) );
			final ConverterSetup setup = getSetupById( id );
			setup.setDisplayRange( min, max );
			setup.setColor( new ARGBType( color ) );
			final MinMaxGroupPG group = minMaxGroups.get( groupId );
			setupToGroup.put( setup, group );
			group.addSetup( setup );
		}

		if ( updateListener != null )
			updateListener.update();
	}

	private ConverterSetup getSetupById( final int id )
	{
		for ( final ConverterSetup setup : setups )
			if ( setup.getSetupId() == id )
				return setup;
		return null;
	}

	private static final WeakHashMap< SetupAssignmentsPG, Integer > maxIds = new WeakHashMap<>(  );

	private static final SetupAssignmentsPG nullSetupAssignmentsKey = new SetupAssignmentsPG( new ArrayList<>(), 0, 1 );

	/**
	 * Get a {@link ConverterSetup#getSetupId() setup id} that is not used in
	 * the specified {@code setupAssignments}.
	 *
	 * @param setupAssignments
	 *            the {@link SetupAssignments} for which to find an unused id.
	 *            May be {@code null}, in which case a new id is returned that
	 *            was not used in any previous call to
	 *            {@code getUnusedSetupId(null)}.
	 *
	 * @return a unused setup id
	 */
	public static synchronized int getUnusedSetupId( SetupAssignmentsPG setupAssignments )
	{
		if ( setupAssignments == null )
			setupAssignments = nullSetupAssignmentsKey;
		int maxId = maxIds.getOrDefault( setupAssignments, 0 );
		for ( final ConverterSetup setup : setupAssignments.getConverterSetups() )
			maxId = Math.max( setup.getSetupId(), maxId );
		++maxId;
		maxIds.put( setupAssignments, maxId );
		return maxId;
	}

}
