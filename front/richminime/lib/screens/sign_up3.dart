import 'package:flutter/material.dart';
import 'package:richminime/screens/sign_up4.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:flutter/services.dart';
import 'package:percent_indicator/linear_percent_indicator.dart';

class SignUp3 extends StatefulWidget {
  final String code;
  const SignUp3({required this.code, Key? key}) : super(key: key);

  @override
  State<SignUp3> createState() => _SignUp3State();
}

class CardNumberInputFormatter extends TextInputFormatter {
  @override
  TextEditingValue formatEditUpdate(
      TextEditingValue oldValue, TextEditingValue newValue) {
    String newText = newValue.text.replaceAll('-', '');
    if (newText.length > 4) {
      newText =
          '${newText.substring(0, 4)}-${newText.substring(4, newText.length)}';
    }
    if (newText.length > 9) {
      newText =
          '${newText.substring(0, 9)}-${newText.substring(9, newText.length)}';
    }
    if (newText.length > 14) {
      newText =
          '${newText.substring(0, 14)}-${newText.substring(14, newText.length)}';
    }

    return TextEditingValue(
      text: newText,
      selection: TextSelection.collapsed(offset: newText.length),
    );
  }
}

class _SignUp3State extends State<SignUp3> {
  TextEditingController cardEmailController = TextEditingController();
  TextEditingController cardPasswordController = TextEditingController();
  TextEditingController cardNumController = TextEditingController();
  Future<void> onNextButtonTap() async {
    // Navigator.push(
    //   context,
    //   MaterialPageRoute(
    //     builder: (context) => SignUp4(
    //       organization: widget.code,
    //       cardNumber: cardNumController.text,
    //     ),
    //   ),
    // );

    final url = Uri.parse("http://10.0.2.2:8080/api/user/connected-id");
    print(widget.code);
    final response = await http.post(
      url,
      headers: {"Content-Type": "application/json"},
      body: jsonEncode(
        {
          "id": cardEmailController.text,
          "password": cardPasswordController.text,
          "organization": widget.code,
          "cardNumber": cardNumController.text,
        },
      ),
    );

    if (response.statusCode == 201) {
      if (!context.mounted) return;
      Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => SignUp4(
            uuid: jsonDecode(response.body)['uuid'],
            organization: widget.code,
            cardNumber: cardNumController.text,
          ),
        ),
      );
    } else {
      AlertDialog(
        title: Text(response.body.toString()),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      // grid 같은 레이아웃을 사용하기 위해 Scaffold 위젯 사용
      backgroundColor: Theme.of(context).colorScheme.background, // 배경색 지정
      body: SingleChildScrollView(
        scrollDirection: Axis.vertical,
        child: Column(
          // Column 위젯을 사용하여 위젯을 세로로 배치
          children: [
            const SizedBox(height: 20),
            LinearPercentIndicator(
              alignment: MainAxisAlignment.center,
              width: MediaQuery.of(context).size.width,
              animation: true,
              animationDuration: 1200,
              lineHeight: 30,
              percent: 0.75,
              center: const Text('3/4'),
              barRadius: const Radius.circular(16),
              progressColor: Colors.red[200],
            ),
            const SizedBox(height: 100),
            Text(
              '카드사 정보를 입력해주세요',
              style: TextStyle(
                fontSize: 25,
                fontWeight: FontWeight.w900,
                color: Theme.of(context).textTheme.bodyLarge?.color,
              ),
            ),
            const SizedBox(height: 20),
            Container(
              alignment: Alignment.topCenter,
              child: SizedBox(
                width: 300,
                child: Column(
                  children: [
                    TextField(
                      controller: cardEmailController,
                      decoration: const InputDecoration(
                        border: InputBorder.none,
                        contentPadding:
                            EdgeInsets.symmetric(vertical: 10, horizontal: 10),
                        labelText: '아이디',
                        fillColor: Color(0xFFFFFDFD),
                        filled: true,
                      ),
                    ),
                    const SizedBox(height: 10),
                    TextField(
                      controller: cardPasswordController,
                      obscureText: true, // 비밀번호 숨기기 옵션
                      decoration: const InputDecoration(
                        border: InputBorder.none,
                        contentPadding:
                            EdgeInsets.symmetric(vertical: 10, horizontal: 10),
                        labelText: '비밀번호',
                        fillColor: Color(0xFFFFFDFD),
                        filled: true,
                      ),
                    ),
                    const SizedBox(height: 10),
                    TextField(
                      controller: cardNumController,
                      keyboardType: TextInputType.number,
                      maxLength: 19,
                      inputFormatters: [CardNumberInputFormatter()],
                      decoration: const InputDecoration(
                        border: InputBorder.none,
                        contentPadding:
                            EdgeInsets.symmetric(vertical: 10, horizontal: 10),
                        labelText: '카드번호',
                        fillColor: Color(0xFFFFFDFD),
                        filled: true,
                        counterText: "",
                      ),
                    ),
                    const SizedBox(height: 20),
                    GestureDetector(
                      onTap: onNextButtonTap,
                      child: Container(
                        alignment: Alignment.center,
                        width: 110,
                        height: 50,
                        decoration: const BoxDecoration(
                          color: Color(0xFFFFBEBE),
                        ),
                        child: const Text(
                          "다음",
                          style: TextStyle(
                            color: Colors.white,
                            fontSize: 15,
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
