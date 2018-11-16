# < Smart Bus System > Capston Design 1 

<p> Environment : Vi Editor in Raspberry Pi/Linux, Android Studio in Windows </p>
<p> Language : C, Java, Python </p>

<br><br>

<p> 개발 배경 </p>
- 버스나 지하철을 이용할 시 카드/스마트폰을 태그하여 요금을 지불함 (NFC) -> 카드/폰을 꺼내야 하는 불편함과 가끔 잊어버릴 때도 있음
- 버스에서 내릴 때 벨을 눌러야 하는 점 -> 버스가 이동 중일 때 일어서야 하는 위험함
- 도착 정류장 -> 딴짓하다가 내려야 할 곳을 놓칠 경우, 초행길에는 매 정류장마다 주의를 기울일 필요가 있음

<p> 기대 효과 </p> 
- 본인과 카드를 인증하기 때문에 할인 카드 부정 사용 감소 가능
- 버스 출/입구를 통과하지 않으면 결제 불가 -> 하차 전 태그 방지 가능
- 카메라(얼굴 인식) 이용 -> 상습 부정 승차 인물 확인 가능
<br>=> 약 9%이상의 부정 승차를 방지 할 수 있을 것!

##### Raspberry Pi 외부 구성
![2](https://user-images.githubusercontent.com/21214309/48603650-86d38c80-e9ba-11e8-9fb8-0601d586e4f3.JPG)

- Raspberry Pi3 B+, PIR Sensor, LCD, LED, PiCamera로 구성

#### Program Flow
![111](https://user-images.githubusercontent.com/21214309/48603654-876c2300-e9ba-11e8-9f60-4b693bcf7747.JPG)

- 서울시 버스 정보 Server를 중심으로 User의 APP과 Bus의 Raspberry Pi가 상호작용하여 스마트 시스템을 제공

<br>

##### App과 User간의 Communication
![6](https://user-images.githubusercontent.com/21214309/48603651-86d38c80-e9ba-11e8-8a95-b9fecb30f258.JPG)

##### Raspberry Pi와 App간의 Communication - TCP/IP 통신
![7](https://user-images.githubusercontent.com/21214309/48603652-86d38c80-e9ba-11e8-8583-2eadd47a2e19.JPG)

1) Image를 APP에서 전송
2) User가 센서 통과시 얼굴 비교를 진행
3) 전송받은 Image와 센서 통과시 찍힌 이미지가 동인 인물이라고 판단될 경우 결제를 진행해도 좋다는 메세지 전송
4) 메세지를 받은 APP은 결제 진행
5) 수신하고 있던 Beacon정보를 변환하여 APP에 제공
  - 제공 받은 정보는 버스 루트정보와 버스의 현재 위치

<br>

##### iBeacon Structure
![default](https://user-images.githubusercontent.com/21214309/48603648-863af600-e9ba-11e8-8351-f012c6551407.JPG)

- 해당 구조를 Java(Android)로 해석하여 APP에 정보로 변환 후, 사용됨 <br>

##### Android openAPI
- 공공데이터 포탈에서 서울특별시 버스위치정보 조회 / 노선정보조회 서비스 이용
1) Beacon에서 버스ID와 노선명 제공 - vehId, busRouteNm
2) getBusRouteList에서 busRouteNm을 조회하여 busRouteId를 얻음
3) getStationsByRouteList에서 busRouteId로 노선 리스트를 얻음
4) 노선을 화면에 Draw
5) getBusPosByVehIdItem에서 vehId를 이용해 버스의 실시간 위치 Draw

<br>

Video URL : https://youtu.be/kGTkV1j1gr0 <br>
Paper PDF File : [KCI_FI002364702.pdf](https://github.com/YouMinJung/SmartBusSystem/files/2588253/KCI_FI002364702.pdf)





