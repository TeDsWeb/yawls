package com.blogspot.thedsweb.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.MissingResourceException;
import java.util.logging.Level;

import org.gnome.gdk.Cursor;
import org.gnome.gdk.Event;
import org.gnome.gdk.Pixbuf;
import org.gnome.gtk.Align;
import org.gnome.gtk.Button;
import org.gnome.gtk.Dialog;
import org.gnome.gtk.Frame;
import org.gnome.gtk.Gtk;
import org.gnome.gtk.HBox;
import org.gnome.gtk.HSeparator;
import org.gnome.gtk.Justification;
import org.gnome.gtk.Label;
import org.gnome.gtk.MenuItem;
import org.gnome.gtk.MenuItem.Activate;
import org.gnome.gtk.ResponseType;
import org.gnome.gtk.VBox;
import org.gnome.gtk.Widget;
import org.gnome.gtk.Window;

import com.blogspot.thedsweb.engine.Brightness;
import com.blogspot.thedsweb.main.Debug;
import com.blogspot.thedsweb.util.Config;
import com.blogspot.thedsweb.util.Database;
import com.blogspot.thedsweb.util.LockFile;
import com.blogspot.thedsweb.util.Translate;

public class CalibGUI {
    private final MenuItem calib;
    private final Window mainGUI;

    public CalibGUI(Window mainGUI, MenuItem calib) {
	this.calib = calib;
	this.mainGUI = mainGUI;
    }

    public void start() {
	calib.connect(new Activate() {
	    @Override
	    public void onActivate(MenuItem arg0) {
		mainGUI.setSensitive(false);
		final Dialog calibrate = new Dialog();
		Pixbuf icon;
		try {
		    icon = new Pixbuf(Database.PATH_ICON);
		    calibrate.setIcon(icon);
		} catch (final FileNotFoundException e3) {
		    Debug.LOG.log(Level.WARNING, "Could not find yawls icon.");
		}

		calibrate.setSizeRequest(300, 0);
		calibrate.setResizable(false);
		calibrate.setBorderWidth(5);

		calibrate.setTitle("Yawls");
		final HBox hbox1 = new HBox(false, 5);
		calibrate.add(hbox1);

		final Frame frame = new Frame(null);
		final Label head = new Label("<b>"
			+ Translate._("Calibration wizard") + "</b>");
		head.setUseMarkup(true);
		frame.setLabelWidget(head);
		hbox1.add(frame);

		final VBox vbox = new VBox(false, 5);
		vbox.setBorderWidth(5);
		frame.add(vbox);

		final Label step1 = new Label("<b>" + Translate._("Step 1:")
			+ "</b>");
		step1.setUseMarkup(true);
		step1.setAlignHorizontal(Align.START);
		vbox.add(step1);
		final Label instruct1 = new Label(
			Translate
				._("Make your environment as bright as possible\nand press the calibrate button."));
		vbox.add(instruct1);
		final Label step2 = new Label("<b>" + Translate._("Step 2:")
			+ "</b>");
		step2.setUseMarkup(true);
		step2.setAlignHorizontal(Align.START);
		vbox.add(step2);
		final Label instruct2 = new Label(
			Translate
				._("Make your environment as dark as possible\nand press the calibrate button again."));
		vbox.add(instruct2);

		final HSeparator hsep = new HSeparator();
		vbox.add(hsep);

		final Label status = new Label();
		status.setUseMarkup(true);
		status.setAlignHorizontal(Align.CENTER);
		status.setJustify(Justification.CENTER);
		vbox.add(status);

		final HBox hbox2 = new HBox(false, 0);
		hbox2.setAlignVertical(Align.END);
		vbox.add(hbox2);

		final Label lastValue = new Label("<b>"
			+ Translate._("Last value:") + "</b>");
		lastValue.setUseMarkup(true);
		lastValue.setAlignHorizontal(Align.END);
		hbox2.add(lastValue);
		final Label value = new Label();
		value.setUseMarkup(true);
		value.setAlignHorizontal(Align.CENTER);
		hbox2.add(value);

		final Button finish = new Button();
		finish.setLabel(Translate._("Finish"));
		calibrate.addButton(finish, ResponseType.NONE);

		final Button back = new Button();
		back.setLabel(Translate._("Back"));
		calibrate.addButton(back, ResponseType.NONE);

		final Button next = new Button();
		next.setLabel(Translate._("Next"));
		calibrate.addButton(next, ResponseType.NONE);

		final Button apply = new Button();
		apply.setLabel(Translate._("Calibrate"));
		calibrate.addButton(apply, ResponseType.NONE);

		final File file = new File(Database.PATH_LOCK_DAEMON);
		LockFile.create(Database.PATH_LOCK_DAEMON, true);

		finish.connect(new Button.Clicked() {
		    @Override
		    public void onClicked(Button arg0) {
			calibrate.hide();
			mainGUI.setSensitive(true);
			file.delete();
		    }
		});

		back.connect(new Button.Clicked() {
		    @Override
		    public void onClicked(Button arg0) {
			finish.hide();
			back.hide();
			next.show();
			step2.hide();
			instruct2.hide();
			step1.show();
			instruct1.show();
		    }
		});

		next.connect(new Button.Clicked() {
		    @Override
		    public void onClicked(Button arg0) {
			next.hide();
			finish.show();
			back.show();
			step1.hide();
			instruct1.hide();
			step2.show();
			instruct2.show();
		    }
		});

		apply.connect(new Button.Clicked() {
		    @Override
		    public void onClicked(Button arg0) {
			calibrate.getWindow().setCursor(Cursor.BUSY);
			Gtk.mainIterationDo(false);
			try {
			    final Config config = new Config();
			    final Brightness brightness = new Brightness(config);
			    final int current = brightness
				    .calibrateBrightness();
			    if (current == -1) {
				status.setLabel("<b>"
					+ Translate._("Backlight detected:")
					+ "\n"
					+ "Please retry it in another position."
					+ "</b>");
			    } else {
				status.setLabel("");
				value.setLabel(Integer.toString(current));
			    }
			} catch (final MissingResourceException e) {
			    Debug.LOG.log(Level.SEVERE,
				    "Could not find config file.", e);
			} catch (final MalformedURLException e1) {
			    Debug.LOG.log(Level.SEVERE,
				    "Could not read config file.", e1);
			}
			calibrate.getWindow().setCursor(Cursor.NORMAL);
		    }
		});

		hbox1.show();
		frame.show();
		head.show();
		vbox.show();
		step1.show();
		instruct1.show();
		step2.hide();
		instruct2.hide();
		hsep.show();
		hbox2.show();
		status.show();
		lastValue.show();
		value.show();
		finish.hide();
		back.hide();
		next.show();
		apply.show();
		calibrate.show();
		calibrate.connect(new Window.DeleteEvent() {
		    @Override
		    public boolean onDeleteEvent(Widget arg0, Event arg1) {
			calibrate.hide();
			mainGUI.setSensitive(true);
			file.delete();
			return false;
		    }
		});
	    }
	});
    }
}
