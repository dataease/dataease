/*
 * A light-weight implementation of a tri-state checkbox providing a div with TriStateCheckBox styling.
 * Using a tri-state item can essentially save the memory in the case of a massive creation of check boxes.
 *
 * THIS IS NOT A WIDGET!!!
 *
 * The tri-state item provides a dom node with tri-state styling specified in ./themes/common/TriStateItem.css.
 * The 'checked' and 'disabled' properties affect on the item's dom node styling.
 * They should be changed using the 'set' method.
 *
 * Additionally, the toggle() method allows toggle the current state of the item to the opposite value.
 * If the current state is 'mixed', the previous state of the item will be toggled.
 *
 * A tri-state item internally ignores mouse clicks when it is in the disabled mode.
 * So, please connent to the 'onClick' method instead of direct listening to the mouse click on the item's DOM node.  
 */

import Stateful = require("dojo/Stateful");

interface TriStateItem extends DojoDeclaredClass {}
interface TriStateItem extends Stateful {}

declare class TriStateItem {
  /**
   * Dom node with item's div.
   */
  domNode: HTMLElement;
  
  /**
   * The dom node for the checkbox.
   */
  checkboxNode: HTMLElement;
  
  /**
   * Checkbox checked state.
   */
  checked: boolean | "mixed";
  
  /**
   * Checkbox disabling mode.
   */
  disabled: boolean;
  
  /**
   * Auto toggle checkbox on click or not.
   */
  autoToggle: boolean;
  
  /**
   * Constructs a new 'div' DOM node with the given attributes.
   * By default the class of this DOM node is "esriTriStateCheckBoxIcon", but other classes could be 
   * also added in the attributes of the constructor.
   * The optional 'refNode' and 'pos' parameters have the same meaning as in domConstruct.create method.
   * If the 'refNode' is a label element, a click on it will be interpreted as a click on this item.
   * if you specify attrs.label as String, a new label will be created with a check box in it.
   * In this case the label becomes the domNode of TriStateItem.
   * You can also specify attrs.labelAttrs - attributes to be applied to the new label.
   */
  constructor(
    refNode? : string | HTMLElement | null,
    attrs? : ({ [s: string] : any } & {
      label? : string,
      labelAttrs?: { [s: string] : any }
    }) | null,
    pos? : "first" | "after" | "before" | "last" | "replace" | "only"
  );
  
  // Ignore parameters assignment for this stateful.
  postscript() : void;
  
  /**
   * On click connector function.
   * The event.checked property contains the current checked state of this item.
   * In the autoToggle mode, it is always a Boolean value.
   * If autoToggle mode is off, it is the current checked state of this item before click.
   */
  onClick(event : Event) : void;
  
  /**
   * Toggles the current state to the opposite value.
   * After applying this method, the state will be either true or false.
   * Returns the new state.
   */
  toggle() : boolean;
}

export = TriStateItem;
