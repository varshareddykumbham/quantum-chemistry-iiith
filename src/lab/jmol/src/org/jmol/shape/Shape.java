/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2011-01-23 07:24:52 +0100 (dim., 23 janv. 2011) $
 * $Revision: 15021 $

 *
 * Copyright (C) 2003-2005  The Jmol Development Team
 *
 * Contact: jmol-developers@lists.sf.net
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jmol.shape;

//import org.jmol.util.Escape;
import org.jmol.util.Logger;
import org.jmol.util.Point3fi;
//import org.jmol.util.XmlUtil;
import org.jmol.viewer.JmolConstants;
import org.jmol.viewer.StateManager;
import org.jmol.viewer.Viewer;
import org.jmol.atomdata.RadiusData;
import org.jmol.g3d.*;
import org.jmol.modelset.Atom;
import org.jmol.modelset.Bond;
import org.jmol.modelset.Group;
import org.jmol.modelset.ModelSet;

import javax.vecmath.Point3f;
import javax.vecmath.Point3i;

import java.util.BitSet;
import java.util.List;
import java.util.Hashtable;

/**
 * Shape now encompasses:
 * 
 * AtomShape (abstract)
 *     |
 *   Balls, Dots, Ellipsoids, Halos, Labels, Polyhedra, Stars, Vectors
 *           |
 *         GeoSurface  
 * 
 * BioShapeCollection (abstract)
 *     |
 *   Backbone, Cartoon, MeshRibbon, Ribbons, Rockets, Strands, Trace
 * 
 * Dipoles
 * 
 * FontLineShape (abstract)
 *     |
 *   Axes, Bbcage, Frank, Uccage
 * 
 * Measures
 * 
 * MeshCollection (abstract)
 *   |       |
 * Draw   Isosurface
 *           |
 *        LcaoCartoon, MolecularOrbital, Pmesh
 * 
 * Sticks
 *     |
 *    Hsticks, Sssticks
 * 
 * TextShape (abstract)
 *     |
 *    Echo, Hover
 *    
 */
public abstract class Shape {

  //public Shape () {
  //  System.out.println("Shape " + this + " constructed");
  //}
  
  //public void finalize() {
  //  System.out.println("Shape " + shapeID + " " + this + " finalized");
  //}
  
  public static final float RADIUS_MAX = 4;
  public Viewer viewer; //public for now for Backbone
  public ModelSet modelSet;
  public Graphics3D g3d;
  public int shapeID;
  public int myVisibilityFlag;
  protected float translucentLevel;
  protected boolean translucentAllowed = true;
  public boolean isBioShape;
  
  public Viewer getViewer() {
    return viewer;
  }
  
  final public void initializeShape(Viewer viewer, Graphics3D g3d, ModelSet modelSet,
                               int shapeID) {
    this.viewer = viewer;
    this.g3d = g3d;
    this.shapeID = shapeID;
    this.myVisibilityFlag = JmolConstants.getShapeVisibilityFlag(shapeID);
    setModelSet(modelSet);
    initShape();
    //System.out.println("Shape " + shapeID + " " + this + " initialized");

  }

  public void setModelSet(ModelSet modelSet) {
    this.modelSet = modelSet;
    initModelSet();
  }
  
  protected void initModelSet() {
  }

  public void initShape() {
  }

  public void merge(Shape shape) {
    // shape-dependent Jmol 12.0.RC6
  }
  
  protected List xmlProperties;
  
  public void setShapeSize(int size, RadiusData rd, BitSet bsSelected) {
    if (Logger.debuggingHigh)
      setXmlProperty("size", (rd == null ? new Integer(size) : (Object) rd),
          bsSelected);
    if (rd == null)
      setSize(size, bsSelected);
    else
      setSize(rd, bsSelected);
  }

  protected void setSize(int size, BitSet bsSelected) {
    // not for atoms except to turn off -- size = 0
  }

  protected void setSize(RadiusData rd, BitSet bsSelected) {
    // balls, dots, other atomshapes
  }

  /**
   * specifically from modelSet.setShapeProperty, mostly from ScriptEvaluation,
   * but not always  -- definitely not from "super.setProperty"
   * 
   * @param propertyName
   * @param value
   * @param bsSelected
   */
  public void setShapeProperty(String propertyName, Object value,
                               BitSet bsSelected) {
    if (!setXmlProperty(propertyName, value, bsSelected))
      setProperty(propertyName, value, bsSelected == null ? 
          viewer.getSelectionSet(false) : bsSelected);
  }

  /**
   * may NOT be over-ridden by shape; executed BEFORE shape's setProperty
   * 
   * @param propertyName
   * @param value
   * @param bs
   * @return true if we are done
   */
  private boolean setXmlProperty(String propertyName, Object value, BitSet bs) {
    
    // xmlProperties is not implemented. 
    // I thought this might be a prelude to an XML-based state
    // but it turns out that is problematic because some of
    // these are HUGE (isosurface, for example, passes incredible 
    // amounts of information via the setProperty mechanism.
    // So "initXml" is never actuated, and xmlProperties is always null.
    String myType = JmolConstants.shapeClassBases[shapeID];
    /*
    if (propertyName == "initXml") {
      xmlProperties = new ArrayList();
      return true;
    }
    if (propertyName == "showXml") {
      if (xmlProperties != null) {
        String s = getXmlPropertyString(xmlProperties, myType);
        if (s != null)
          Logger.info(s);
      }
      return true;
    }
    if (propertyName == "setXml") {
      setXmlProperty("showXml", null, null);
      return false;
    }
    */
    if (Logger.debuggingHigh && shapeID != JmolConstants.SHAPE_HOVER)
      Logger.info(myType + " setProperty: " + propertyName + " = " + value);

    /*
    if (xmlProperties == null)
      return false;

    if (propertyName == "setProperties" || propertyName == "thisID")
      return false;
    List attributes = new ArrayList();
    attributes.add(new Object[] { "select",
        bs == null ? null : Escape.escape(bs) });
    xmlProperties.add(XmlUtil
        .escape(propertyName, attributes, value, false, ""));
        
    */
    return false;
  }

/*
  static private String getXmlPropertyString(List xmlProperties, String type) {
    if (xmlProperties == null || xmlProperties.size() == 0)
      return null;
    StringBuffer sb = new StringBuffer();
    XmlUtil.openTag(sb, "shape", new String[] { "type", type });
    XmlUtil.toXml(sb, "property", xmlProperties);
    XmlUtil.closeTag(sb, "shape");
    return sb.toString();
  }
*/
  /**
   * may come from any source -- executed AFTER a shape's own setProperty method
   * 
   * @param propertyName
   * @param value
   * @param bsSelected
   */
  public void setProperty(String propertyName, Object value, BitSet bsSelected) {
/*    if (propertyName == "setXml") {
      // some states mignt also check this in order to preseve their state
      xmlProperties = new ArrayList();
      return;
    }
*/
    if (propertyName == "setProperties") {
      List propertyList = (List) value;
      while (propertyList.size() > 0) {
        Object[] data = (Object[]) propertyList.remove(0);
        setShapeProperty(((String) data[0]).intern(), data[1], null);
      }
      return;
    }
    if (propertyName == "translucentLevel") {
      translucentLevel = ((Float) value).floatValue();
      return;
    }

    if (propertyName == "refreshTrajectories") {
      return;
    }

    Logger.warn("unassigned " + JmolConstants.shapeClassBases[shapeID] + " + shape setProperty:" + propertyName + ":" + value);
  }

  public boolean getProperty(String property, Object[] data) {
    return false;
  }

  public Object getProperty(String property, int index) {
    return null;
  }

  public int getIndexFromName(String thisID) {
    return -1;
  }

  public boolean wasClicked(int x, int y) {
    return false;
  }

  public void findNearestAtomIndex(int xMouse, int yMouse, Atom[] closest, BitSet bsNot) {
  }

  public void checkBoundsMinMax(Point3f pointMin, Point3f pointMax) {
  }

  public void setModelClickability() {
  }

  public Point3fi checkObjectClicked(int x, int y, int modifiers, BitSet bsVisible) {
    return null;
  }

  public boolean checkObjectHovered(int x, int y, BitSet bsVisible) {
    return false;
  }

  public boolean checkObjectDragged(int prevX, int prevY, int x, int y,
                             int modifiers, BitSet bsVisible) {
    return false;
  }

  protected int coordinateInRange(int x, int y, Point3f vertex, int dmin2, Point3i ptXY) {
    viewer.transformPoint(vertex, ptXY);
    int d2 = (x - ptXY.x) * (x - ptXY.x) + (y - ptXY.y) * (y - ptXY.y);
    return (d2 < dmin2 ? d2 : -1);
  }
  
  public short setColix(short colix, byte paletteID, int atomIndex) {
    return setColix(colix, paletteID, modelSet.atoms[atomIndex]);
  }

  protected short setColix(short colix, byte paletteID, Atom atom) {
    return (colix == Graphics3D.USE_PALETTE ? viewer.getColixAtomPalette(atom,
        paletteID) : colix);
  }

  protected short setColix(short colix, byte paletteID, Bond bond) {
    return (colix == Graphics3D.USE_PALETTE ? viewer.getColixBondPalette(bond,
        paletteID) : colix);
  }

  public List getShapeDetail() {
    return null;
  }

  public String getShapeState() {
    return null;
  }

  public void setVisibilityFlags(BitSet bs) {
  }

  static public void setStateInfo(Hashtable ht, int i, String key) {
    setStateInfo(ht, i, i, key);
  }

  static public void setStateInfo(Hashtable ht, int i1, int i2, String key) {
    StateManager.setStateInfo(ht, i1, i2, key);
  }

  static public String getShapeCommands(Hashtable htDefine, Hashtable htMore) {
    return StateManager.getCommands(htDefine, htMore);
  }

  static public String getShapeCommands(Hashtable htDefine, Hashtable htMore,
                                 String selectCmd) {
    return StateManager.getCommands(htDefine, htMore, selectCmd);
  }

  static public void appendCmd(StringBuffer s, String cmd) {
    StateManager.appendCmd(s, cmd);
  }

  static public String getFontCommand(String type, Font3D font) {
    if (font == null)
      return "";
    return "font " + type + " " + font.fontSizeNominal + " " + font.fontFace + " "
        + font.fontStyle;
  }

  public String getColorCommand(String type, short colix) {
    return getColorCommand(type, JmolConstants.PALETTE_UNKNOWN, colix);
  }

  public String getColorCommand(String type, byte pid, short colix) {
    if (pid == JmolConstants.PALETTE_UNKNOWN && colix == Graphics3D.INHERIT_ALL)
      return "";
    return "color " + type + " " + encodeTransColor(pid, colix, translucentAllowed);
  }

  private static String encodeTransColor(byte pid, short colix,
                                  boolean translucentAllowed) {
    if (pid == JmolConstants.PALETTE_UNKNOWN && colix == Graphics3D.INHERIT_ALL)
      return "";
    /* nuance here is that some palettes depend upon a
     * point-in-time calculation that takes into account
     * some aspect of the current state, such as what groups
     * are selected in the case of "color group". So we have
     * to identify these and NOT use them in serialization.
     * Serialization of the palette name is just a convenience
     * anyway. 
     */
    return (translucentAllowed ? getTranslucentLabel(colix) + " " : "")
        + (pid != JmolConstants.PALETTE_UNKNOWN 
        && !JmolConstants.isPaletteVariable(pid) 
        ? JmolConstants.getPaletteName(pid) : encodeColor(colix));
  }

  static String encodeColor(short colix) {
    // used also by labels for background state (no translucent issues there?)
    return (Graphics3D.isColixColorInherited(colix) ? "none" : Graphics3D
        .getHexCode(colix));
  }

  private static String getTranslucentLabel(short colix) {
    return (Graphics3D.isColixTranslucent(colix) ? "translucent "
        + Graphics3D.getColixTranslucencyLevel(colix): "opaque");
  }

  public static short getColix(short[] colixes, int i, Atom atom) {
    return Graphics3D.getColixInherited(
        (colixes == null || i >= colixes.length ? Graphics3D.INHERIT_ALL
            : colixes[i]), atom.getColix());
  }

  public int getSize(int atomIndex) {
    return 0;
  }

  public int getSize(Group group) {
    return 0;
  }

}
