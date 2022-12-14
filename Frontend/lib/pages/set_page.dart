import 'package:flutter/material.dart';
import 'package:homealone/components/set/circular_menu.dart';
import 'package:homealone/components/set/set_button.dart';

class SetPage extends StatefulWidget {
  const SetPage({Key? key}) : super(key: key);

  @override
  State<SetPage> createState() => _SetPageState();
}

class _SetPageState extends State<SetPage> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      resizeToAvoidBottomInset: false,
      body: Column(
        children: [
          Flexible(
            flex: 2,
            child: SetButton(),
          ),
          Flexible(
            flex: 1,
            child: CircularMenu(),
          ),
        ],
      ),
    );
  }
}
