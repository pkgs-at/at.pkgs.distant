(function(root, unasigned) {
	root.BEGIN_CLIENT_TEMPLATE = '<script type="application/x-template">';
	root.END_CLIENT_TEMPLATE = '</script>';
	root._ = {
		bs3_alert_classes: {
			ERROR: 'alert-danger',
			WARNING: 'alert-warning',
			NOTICE: 'alert-info',
			SUCCESS: 'alert-success',
		},
		bs3_alert_class: function(level) {
			if (level instanceof java.lang.Enum) level = level.name();
			return this.bs3_alert_classes[level];
		},
	};
})(this);
