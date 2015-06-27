/*******************************************************************************
 * Copyright (c) 2013-2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.logging.Level;

import org.gnome.gdk.Cursor;
import org.gnome.gdk.Event;
import org.gnome.gdk.Pixbuf;
import org.gnome.gtk.AboutDialog;
import org.gnome.gtk.Align;
import org.gnome.gtk.Button;
import org.gnome.gtk.Dialog;
import org.gnome.gtk.Frame;
import org.gnome.gtk.Gtk;
import org.gnome.gtk.HBox;
import org.gnome.gtk.HButtonBox;
import org.gnome.gtk.HSeparator;
import org.gnome.gtk.Justification;
import org.gnome.gtk.Label;
import org.gnome.gtk.License;
import org.gnome.gtk.Menu;
import org.gnome.gtk.MenuBar;
import org.gnome.gtk.MenuItem;
import org.gnome.gtk.MenuItem.Activate;
import org.gnome.gtk.ResponseType;
import org.gnome.gtk.SpinButton;
import org.gnome.gtk.Statusbar;
import org.gnome.gtk.Switch;
import org.gnome.gtk.VBox;
import org.gnome.gtk.Widget;
import org.gnome.gtk.Window;
import org.gnome.gtk.Window.DeleteEvent;
import org.gnome.gtk.WindowPosition;

import com.blogspot.thedsweb.engine.Brightness;
import com.blogspot.thedsweb.main.Debug;
import com.blogspot.thedsweb.util.Check;
import com.blogspot.thedsweb.util.Config;
import com.blogspot.thedsweb.util.Database;
import com.blogspot.thedsweb.util.Files;
import com.blogspot.thedsweb.util.LockFile;
import com.blogspot.thedsweb.util.Translate;

public class Show {
    private static Window mainGUI;
    private static Statusbar status;
    private static Switch fdSwitch;
    private static SpinButton llSpin;
    private static SpinButton ulSpin;
    private static SpinButton miInSpin;
    private static SpinButton maInSpin;
    private static SpinButton dtSpin;
    private static SpinButton btSpin;
    private static SpinButton loSpin;
    private static Label minValue;
    private static Label maxValue;
    private static Label lastValue;

    public static void gui(String[] args) {
	Gtk.init(args);
	mainGUI = new Window();
	mainGUI.setSizeRequest(550, 0);
	mainGUI.setResizable(false);
	mainGUI.setTitle("Yawls");
	mainGUI.setPosition(WindowPosition.CENTER);
	mainGUI.setBorderWidth(5);
	Pixbuf icon;
	try {
	    icon = new Pixbuf(Database.PATH_ICON);
	    mainGUI.setIcon(icon);
	} catch (final FileNotFoundException e) {
	    Debug.LOG.log(Level.WARNING, "Could not find yawls icon.");
	}

	status = new Statusbar();

	final VBox ground = new VBox(false, 4);

	// Create MenuBar
	final MenuBar menuBar = new MenuBar();
	constructMenu(menuBar);

	final HBox cage = new HBox(false, 2);

	final Frame frame1 = new Frame(null);
	final Label stats = new Label("<b>" + Translate._("Webcam stats:")
		+ "</b>");
	stats.setUseMarkup(true);
	frame1.setLabelWidget(stats);
	constructWebcamStats(frame1);
	frame1.setBorderWidth(1);
	cage.add(frame1);

	final Frame frame2 = new Frame(null);
	final Label conf = new Label("<b>" + Translate._("Configuration:")
		+ "</b>");
	conf.setUseMarkup(true);
	frame2.setLabelWidget(conf);
	constructConfiguration(frame2);
	frame2.setBorderWidth(1);
	cage.add(frame2);

	final HButtonBox buttonBox = new HButtonBox();
	constructButton(buttonBox);

	status.setMessage(checkStatus());

	ground.packStart(menuBar, false, false, 0);
	ground.packStart(cage, false, false, 0);
	ground.packStart(buttonBox, false, false, 0);
	ground.packEnd(status, false, false, 0);

	mainGUI.add(ground);

	mainGUI.connect(new DeleteEvent() {
	    @Override
	    public boolean onDeleteEvent(Widget arg0, Event arg1) {
		Gtk.mainQuit();
		return false;
	    }
	});

	mainGUI.showAll();
	Gtk.main();
    }

    private static String checkStatus() {
	String status = "";
	if (!Check.backlightDevice()) {
	    status = Translate._("No backlight device found");
	    if (!Check.camera()) {
		status += " ";
		return status + Translate._("and no camera found.");
	    }
	    status += ".";
	} else if (!Check.camera()) {
	    return Translate._("No camera found.");
	}
	return status;
    }

    private static void constructButton(HButtonBox buttonBox) {
	final Button reset = new Button();
	reset.setLabel(Translate._("Reset"));
	reset.connect(new Button.Clicked() {
	    @Override
	    public void onClicked(Button arg0) {
		Files.writeConfig();
	    }
	});
	buttonBox.add(reset);
	final Button apply = new Button();
	apply.setLabel(Translate._("Apply"));
	apply.connect(new Button.Clicked() {
	    @Override
	    public void onClicked(Button arg0) {
		final boolean fd = fdSwitch.isActive();
		final int ll = (int) llSpin.getValue();
		final int ul = (int) ulSpin.getValue();
		final int mi = (int) miInSpin.getValue();
		final int ma = (int) maInSpin.getValue();
		final int dt = (int) dtSpin.getValue();
		final int bt = (int) btSpin.getValue();

		if (ul <= ll) {
		    status.setMessage(Translate
			    ._("The value of the upper limit must be greater than the one of the lower limit."));
		    return;
		}
		if (mi > ma) {
		    status.setMessage(Translate
			    ._("The value of the max. increase must be greater than the one of the min. increase."));
		    return;
		}
		status.setMessage(checkStatus());

		final Properties config = new Properties();
		config.setProperty("faceDetect", Boolean.toString(fd));
		config.setProperty("lowerLimit", Integer.toString(ll));
		config.setProperty("upperLimit", Integer.toString(ul));
		config.setProperty("minIncrease", Integer.toString(mi));
		config.setProperty("maxIncrease", Integer.toString(ma));
		config.setProperty("darkeningThreshold", Integer.toString(dt));
		config.setProperty("brighteningThreshold", Integer.toString(bt));

		final File cFile = new File(Database.PATH_TO_CONFIG_FILE);
		try {
		    final OutputStream configStream = new FileOutputStream(
			    cFile);
		    config.store(configStream, "Yawls auto generated config");
		} catch (final IOException e) {
		    Debug.LOG.log(Level.WARNING, "Could not create file: "
			    + Database.PATH_TO_CONFIG_FILE, e);
		}

		// Create a lock file to trigger the daemon to
		// reload the configuration on the fly
		LockFile.create(Database.PATH_LOCK_CONFIG, false);
	    }
	});
	buttonBox.add(apply);
	buttonBox.setAlignHorizontal(Align.END);
    }

    private static void constructConfiguration(Frame frame) {
	final VBox vbox = new VBox(false, 5);
	vbox.setBorderWidth(10);
	final HBox hbox1 = new HBox(true, 0);
	hbox1.setSizeRequest(275, 0);
	final HBox hbox2 = new HBox(true, 0);
	hbox2.setSizeRequest(275, 0);
	final HBox hbox3 = new HBox(true, 0);
	hbox3.setSizeRequest(275, 0);
	final HBox hbox4 = new HBox(true, 0);
	hbox4.setSizeRequest(275, 0);
	final HBox hbox5 = new HBox(true, 0);
	hbox5.setSizeRequest(275, 0);
	final HBox hbox6 = new HBox(true, 0);
	hbox6.setSizeRequest(275, 0);
	final HBox hbox7 = new HBox(true, 0);
	hbox7.setSizeRequest(275, 0);
	final HBox hbox8 = new HBox(true, 0);
	hbox8.setSizeRequest(275, 0);

	final Label fdLabel = new Label(Translate._("face detect"));
	fdLabel.setAlignHorizontal(Align.START);
	fdLabel.setTooltipText(Translate
		._("Set true to enable or false to disable face detection"));
	final Label llLabel = new Label(Translate._("lower limit"));
	llLabel.setAlignHorizontal(Align.START);
	llLabel.setTooltipText(Translate
		._("Minimum time between camera activations in milliseconds"));
	final Label ulLabel = new Label(Translate._("upper limit"));
	ulLabel.setAlignHorizontal(Align.START);
	ulLabel.setTooltipText(Translate
		._("Maximum time between camera activations in milliseconds"));
	final Label miInLabel = new Label(Translate._("min. increase"));
	miInLabel.setAlignHorizontal(Align.START);
	miInLabel
		.setTooltipText(Translate
			._("Minimum time increase between camera activations in milliseconds if nothing to do"));
	final Label maInLabel = new Label(Translate._("max. increase"));
	maInLabel.setAlignHorizontal(Align.START);
	maInLabel
		.setTooltipText(Translate
			._("Maximum time increase between camera activations in milliseconds if nothing to do"));
	final Label dtLabel = new Label(Translate._("darkening threshold"));
	dtLabel.setAlignHorizontal(Align.START);
	dtLabel.setTooltipText(Translate
		._("Value for darkening threshold in percent (0-100)\nFor example a value of 25 means that the current brightness\nhas to be less than 75 percent of the last measured brightness."));
	final Label btLabel = new Label(Translate._("brightening threshold"));
	btLabel.setAlignHorizontal(Align.START);
	btLabel.setTooltipText(Translate
		._("Value for brightening threshold in percent (0-100)\nFor example a value of 10 means that the current brightness\nhas to be greater than 110 percent of the last measured brightness."));
	final Label loLabel = new Label(Translate._("log level"));
	loLabel.setAlignHorizontal(Align.START);
	loLabel.setTooltipText(Translate
		._("Log level 0 (error) < 1 (warning) < 2 (info) < 3 (debug)"));

	hbox1.add(fdLabel);
	hbox2.add(llLabel);
	hbox3.add(ulLabel);
	hbox4.add(miInLabel);
	hbox5.add(maInLabel);
	hbox6.add(dtLabel);
	hbox7.add(btLabel);
	hbox8.add(loLabel);

	fdSwitch = new Switch();
	fdSwitch.setAlignHorizontal(Align.START);
	llSpin = new SpinButton(500, 600000, 500);
	llSpin.setAlignHorizontal(Align.FILL);
	ulSpin = new SpinButton(1000, 600000, 500);
	ulSpin.setAlignHorizontal(Align.FILL);
	miInSpin = new SpinButton(100, 600000, 100);
	miInSpin.setAlignHorizontal(Align.FILL);
	maInSpin = new SpinButton(100, 600000, 100);
	maInSpin.setAlignHorizontal(Align.FILL);
	dtSpin = new SpinButton(5, 100, 5);
	dtSpin.setAlignHorizontal(Align.FILL);
	btSpin = new SpinButton(5, 100, 5);
	btSpin.setAlignHorizontal(Align.FILL);
	loSpin = new SpinButton(0, 3, 1);
	loSpin.setAlignHorizontal(Align.FILL);

	try {
	    final Config config = new Config();
	    fdSwitch.setActive(config.faceDetect());
	    llSpin.setValue(config.lowerLimit());
	    ulSpin.setValue(config.upperLimit());
	    miInSpin.setValue(config.minIncrease());
	    maInSpin.setValue(config.maxIncrease());
	    dtSpin.setValue(config.darkeningThreshold());
	    btSpin.setValue(config.brighteningThreshold());
	    loSpin.setValue(config.logLevel());
	} catch (final MissingResourceException e) {
	    Debug.LOG.log(Level.SEVERE, "Could not find config file.", e);
	} catch (final MalformedURLException e1) {
	    Debug.LOG.log(Level.SEVERE, "Could not read config file.", e1);
	}

	hbox1.add(fdSwitch);
	hbox2.add(llSpin);
	hbox3.add(ulSpin);
	hbox4.add(miInSpin);
	hbox5.add(maInSpin);
	hbox6.add(dtSpin);
	hbox7.add(btSpin);
	hbox8.add(loSpin);

	vbox.packStart(hbox1, false, false, 0);
	vbox.packStart(hbox2, false, false, 0);
	vbox.packStart(hbox3, false, false, 0);
	vbox.packStart(hbox4, false, false, 0);
	vbox.packStart(hbox5, false, false, 0);
	vbox.packStart(hbox6, false, false, 0);
	vbox.packStart(hbox7, false, false, 0);
	vbox.packStart(hbox8, false, false, 0);

	frame.add(vbox);
    }

    private static void constructWebcamStats(Frame frame) {
	final VBox vbox = new VBox(false, 5);
	vbox.setBorderWidth(10);
	final HBox hbox1 = new HBox(true, 0);
	final HBox hbox2 = new HBox(true, 0);
	final HBox hbox3 = new HBox(true, 0);

	final Label maxLabel = new Label(Translate._("Max. value:"));
	final Label minLabel = new Label(Translate._("Min. value:"));
	final Label lastLabel = new Label(Translate._("Last value:"));
	maxValue = new Label();
	minValue = new Label();
	lastValue = new Label();

	final Thread lastValueDaemon = new Thread(new ValueDaemon(minValue,
		maxValue, lastValue), "valueDaemon");
	lastValueDaemon.setDaemon(true);
	lastValueDaemon.start();

	hbox1.add(maxLabel);
	hbox1.add(maxValue);

	hbox2.add(minLabel);
	hbox2.add(minValue);

	hbox3.add(lastLabel);
	hbox3.add(lastValue);

	vbox.packStart(hbox1, false, false, 1);
	vbox.packStart(hbox2, false, false, 1);
	vbox.packStart(hbox3, false, false, 1);

	frame.add(vbox);
    }

    private static void constructMenu(MenuBar menuBar) {
        final MenuItem helpItem = new MenuItem(Translate._("_Help"));
        final MenuItem prefItem = new MenuItem(Translate._("_Preferences"));
    
        final Menu helpMenu = new Menu();
        final Menu prefMenu = new Menu();
    
        final MenuItem aboutItem = new MenuItem(Translate._("_About"));
        aboutGUI(aboutItem);
        final MenuItem faqItem = new MenuItem(Translate._("FAQ"));
        faqItem.connect(new Activate() {
            @Override
            public void onActivate(MenuItem arg0) {
        	// Open launchpad.net project questions web page
        	if (java.awt.Desktop.isDesktopSupported()) {
        	    final java.awt.Desktop desktop = java.awt.Desktop
        		    .getDesktop();
    
        	    if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
        		java.net.URI uri;
        		try {
        		    // Set URL for launchpad.net FAQ
        		    uri = new java.net.URI(Database.QUESTIONS);
        		    desktop.browse(uri);
        		} catch (final URISyntaxException e) {
        		    Debug.LOG.log(Level.SEVERE, "Wrong URL syntax: "
        			    + Database.QUESTIONS, e);
        		} catch (final IOException e) {
        		    Debug.LOG.log(Level.WARNING,
        			    "Could not start default browser.", e);
        		}
        	    }
        	}
            }
        });
        final MenuItem calibItem = new MenuItem(Translate._("_Calibrate"));
        calibrateGUI(calibItem);
    
        helpMenu.add(aboutItem);
        helpMenu.add(faqItem);
        prefMenu.add(calibItem);
    
        helpItem.setSubmenu(helpMenu);
        prefItem.setSubmenu(prefMenu);
    
        menuBar.add(helpItem);
        menuBar.add(prefItem);
    }

    private static void calibrateGUI(MenuItem calib) {
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

    private static void aboutGUI(MenuItem about) {
	about.connect(new Activate() {
	    @Override
	    public void onActivate(MenuItem arg0) {
		mainGUI.setSensitive(false);
		final AboutDialog a = new AboutDialog();

		a.setProgramName("Yawls");

		Pixbuf icon;
		try {
		    icon = new Pixbuf(Database.PATH_ICON);
		    a.setIcon(icon);
		    a.setLogo(icon);
		} catch (final FileNotFoundException e) {
		    Debug.LOG.log(Level.WARNING, "Could not find yawls icon.");
		}

		a.setVersion(Database.VERSION);

		a.setComments("Yet Another Webcam Light Sensor");
		a.setWebsite("https://www.launchpad.net/yawls");
		a.setWebsiteLabel("launchpad.net/yawls");

		a.setLicenseType(License.GPL_3_0);

		final String[] authors = { "Dominik Brämer" };
		a.setAuthors(authors);

		a.setTranslatorCredits(Database.CREDITS);

		a.run();
		a.hide();
		mainGUI.setSensitive(true);
	    }
	});
    }
}
