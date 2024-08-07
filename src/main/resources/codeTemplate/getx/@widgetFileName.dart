import 'package:biz/biz/game_center/test/test_controller.dart';
import 'package:flutter/material.dart';
import 'package:service/common/getx/get_abs_state.dart';

class @WidgetClassName extends StatefulWidget {
  const @WidgetClassName({super.key});

  @override
  State<@WidgetClassName> createState() => @WidgetStateClassName();
}

class @WidgetStateClassName extends State<@WidgetClassName> with GetStateBuilderMixin<@WidgetClassName, @ControllerName> {
  @override
  Widget build(BuildContext context) {
    return buildGetWidget(context);
  }

  @override
  Widget buildStatusContent(BuildContext context) {
    return Placeholder();
  }

  @override
  @ControllerName initCtl() {
    return @ControllerName();
  }
}
