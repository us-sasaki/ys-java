---
title: Netcomm
layout: default
---

## �T�v

�ȉ��̃Z�N�V�����ł́ANetComm���[�^�[��Cumulocity�ƕ����Ďg�p������@��������܂��B��̓I�ɁA�ȉ��̍�Ƃ̎菇��������܂��B

* ���[�^�[��[�\���ݒ�](#configure) �B
* ������Cumulocity�A�J�E���g�ւ̃��[�^�[��[�ڑ�](#connect)�B
* [WAN�ALAN�����DHCP�̃p�����[�^�[�̍\���ݒ�](#network)�B
*  [�\�t�g�E�F�A����уt�@�[���E�F�A](#software)�̊Ǘ��B
*  [�V�X�e�����\�[�X](#system)�̊Ď��B
* ����[GPS](#gps)�@�\�̎g�p�B
* ����[GPIO](#gpio)�s���̎g�p�B
* [�\���ݒ�](#rdb)�p�����[�^�[�̉{���B
* �@���[SMS���[�h](#sms_mode)�Ŏg�p����ꍇ�̍\���ݒ�B
* [�@��̃V�F��](#shell)�o�R�ł̃e�L�X�g�R�}���h�̉��u���s�B
* Get [�C�x���g�ʒm](#notifications)�̎擾�B
* [Modbus](#modbus)�@��̐ڑ��B
* [�e�탍�O](#logs)�̉{���B

�ȉ��̃Z�N�V�����ł́ANetComm [�G�[�W�F���g](/guides/devices/netcomm-release)�p�b�P�[�W�����[�^�[�ɃC���X�g�[���ς݂ł���Ƒz�肵�܂��B���̃G�[�W�F���g�� [NTC-6200](http://www.netcommwireless.com/product/m2m/ntc-6200)�����[NTC-140W](http://www.netcommwireless.com/product/4g-wifi-m2m-router)�ƌ݊���������܂��B ���[�^�[���L�̓����ɂ��ďڂ����́A���[�^�[�̃z�[���y�[�W�́uDownloads�i�_�E�����[�h�j�v�Z�N�V�����ɋL�ڂ���Ă��邻�ꂼ��̃}�j���A�����������������B

## <a name="configure"></a>���[�^�[�̍\���ݒ�

Cumulocity�ɑΉ����邽�߂̍\���ݒ�́A���[�^�[�̃E�F�u���[�U�[�C���^�[�t�F�[�X�o�R�ōs�����Ƃ��ł��܂��B���̏ꍇ�A���[�^�[�̃}�j���A���ɋL�ڂ̒ʂ�A���[�U�[�C���^�[�t�F�[�X�փ��O�C�����܂��B�uSystem�i�V�X�e���j�v�^�u�փi�r�Q�[�g���A�uInternet of Things�i���m�̃C���^�[�l�b�g�j�v�̃��j���[���ڂ��N���b�N���܂��B

![Cumulocity�\���ݒ�](/guides/devices/netcomm/routerconf.png)

�uCumulocity agent�iCumulocity�G�[�W�F���g�j�v�̃g�O���X�C�b�`���uON�v�ɐݒ肳��A�uServer�i�T�[�o�[�j�v���ɋL�ڂ�URL�������̐ڑ�������Cumulocity�C���X�^���X��URL�ł��邩�ǂ����A���؂��܂��B��Ƃ��āA�ȉ����g�p���܂��B

* https://developer.cumulocity.com/s �FCumulocity�ւ̐ڑ��B
* https://management.ram.m2m.telekom.com/s �FDeutsche Telekom�ɂ�郂�m�̃C���^�[�l�b�g�ւ̐ڑ��B

�C�ӂŁA�ȉ��̋@�\�����Ƀf�[�^���W��L�������邱�Ƃ��ł��܂��B

* GPIO�A�i���O����F�A�i���O���͓d���𑗐M���܂��i�b�P�ʁj�B
* GPS�ʒu�Ԋu�F���݂�GPS�ʒu���X�V���܂��i�b�P�ʁj�B
* GPS�ʒu�C�x���g�FGPS�ʒu�g���[�X�𑗐M���܂��i�b�P�ʁj�B
* �V�X�e�����\�[�X����FCPU�g�p�󋵁A�������g�p�󋵂���уl�b�g���[�N�g���t�B�b�N�Ɋւ�������擾���܂��i�b�P�ʁj�B

�����̃I�v�V�����͂��ׂāA�����ݒ�ł͖����̏�Ԃł��i�Ԋu��0�ɐݒ肳��Ă��܂��j�B

�E�F�u�C���^�[�t�F�[�X��Cumulocity�ւ̐ڑ���Ԃ�\�����܂��B

�i�o�[�W����2.x�̏ꍇ�j

 * Off�i�I�t�j�F�\�t�g�E�F�A�͖����̏�Ԃł��B
 * Initializing�i���������j�F�\�t�g�E�F�A�����������ł��B
 * Registering�i�o�^���j�F�@���Cumulocity�ւ̓o�^��ҋ@���ł��i���̃Z�N�V�����Q�Ɓj�B
 * Starting�i�N�����j�F�\�t�g�E�F�A�����ׂẴR���|�[�l���g���N�����܂��B
 * No credentials�i�F�؏��Ȃ��j�F�@�킪Cumulocity�ɃA�N�Z�X���邽�߂̔F�؏����擾���Ȃ��������A�F�؏�񂪖����Ƃ��ꂽ���A�܂��͔F�؏�񂪌���Ă��܂����B
 * Started�i�N���ς݁j�F�\�t�g�E�F�A�͋N�����Ă��܂��B
 * Connecting�i�ڑ��������j�F�\�t�g�E�F�A��Cumulocity�֐ڑ��������ł��B
 * Connected�i�ڑ��ς݁j�F�\�t�g�E�F�A��Cumulocity�֐ڑ����ꂽ��Ԃł��B
 * Disconnected�i�ڑ������j�F�\�t�g�E�F�A��Cumulocity�֐ڑ�����Ă��܂���B
 * Reconnecting�i�Đڑ����j�F�\�t�g�E�F�A�͐ڑ����Ď��s���ł��B
 * Stopping�i�I���������j�F�\�t�g�E�F�A�͏I���������ł��B

�i�o�[�W����3.x�̏ꍇ�j
* Checking network connection�i�l�b�g���[�N�ڑ��m�F���j�F�N����ԂŃ��o�C���l�b�g���[�N�ڑ���ҋ@���ł��B
* Bootstrapping�i�u�[�g�X�g���b�v�j�F�F�؏������[�h�A�܂��͔F�؏���Cumulocity�Ƀ��N�G�X�g���܂��B
* Integrating�i�����������j�FCumulocity�֐ڑ��������ł��B
* Loading plugins�i�v���O�C�������[�h���j�FLua�v���O�C�������[�h���ł��B
* Connected�i�ڑ��ς݁j�F�G�[�W�F���g��Cumulocity�ւ̐ڑ��ɐ������܂����B
* No server URL�i�T�[�o�[URL�Ȃ��j�F�T�[�o�[URL�����݂��Ȃ����A�܂��͖����ł��B
* Bootstrap failed�i�u�[�g�X�g���b�v���s�j�FCumulocity����F�؏����擾�ł��܂���B
* Integration failed�i�������s�j�FCumulocity�֐ڑ��ł��܂���B
* Create threads failed�i�X���b�h�쐬���s�j�F���|�[�^�[�̊J�n�܂��͋@��̃v�b�V�����s�����Ƃ��ł��܂���B

## <a name="connect"></a>���[�^�[�̐ڑ�

�����g��NetComm���[�^�[��Cumulocity�ɓo�^����ɂ́A���[�^�[�̐����ԍ����u_�@��ID_�v�Ƃ��Đݒ肷��K�v������܂��B�o�^�菇�̓��[�U�[�K�C�h�́u[Connecting devices�i�@��̐ڑ��j](/guides/users-guide/device-management/#device-registration)�v�Z�N�V�����ɋL�ڂ���Ă��܂��B�����ԍ��͉��L�̒ʂ�A���[�^�[�̌�ʂɈ������Ă��܂��B���邢�́A���[�^�[�̃E�F�u���[�U�[�C���^�[�t�F�[�X�ł��m�F�ł��܂��B�uSystem�i�V�X�e���j�v�փi�r�Q�[�g���A�uInternet of Things�i���m�̃C���^�[�l�b�g�j�v�֐i�݁A�uDevice ID�i�@��ID�j�v�t�B�[���h�����m�F���������B

> �o�[�W����2.x�̃��[�U�[�A�܂���2.x����3.x�փA�b�v�O���[�h���郆�[�U�[�́A���[�^�[��MAC�A�h���X���g�p���Ă��������BMAC�A�h���X����͂���ۂ͕K���A�������Ɛ����̂ݎg�p���Ă��������BMAC�A�h���X���R�����ŋ�؂�Ȃ��ł��������B�Ⴆ�΁A�摜�����MAC�A�h���X�͈ȉ��̂悤�ɓ��͂���邱�ƂɂȂ�܂��B

	006064dda4ae

![MAC�A�h���X](/guides/devices/netcomm/mac.png)

�uaccept�i�����j�v�{�^�����N���b�N������A�uAll devices�i���ׂĂ̋@��j�v�փi�r�Q�[�g����ƁA���[�^�[���o�^��ɂ����֕\�������͂��ł��B���[�^�[�̏����ݒ薼�́u&lt;�^��&gt; (S/N &lt;�����ԍ�&gt;)�v�ł��B�u&lt;�^��&gt;�v�͋@��̌^�������w���܂��B�Ⴆ�΁A��L�̃��[�^�[�̏ꍇ�A�uNTC-6200-02 (S/N 165711141901036)�v�ƕ\������܂��B���[�^�[���N���b�N����ƁA�ڍ׏����{��������A�{���̌㑱�Z�N�V�����ɋL�ڂ̋@�\�փA�N�Z�X�����肷�邱�Ƃ��ł��܂��B�o�^�ς݂̃��[�^�[�����X�g���̑��̋@��Ƌ�ʂ��邽�߁A���[�^�[�����uInfo�i���j�v�^�u��ŕύX���邱�Ƃ��ł��܂��B���̃^�u�ɂ̓��[�^�[�̐����ԍ���SIM�J�[�h�f�[�^�Ȃǂ̊�{�����\������܂��B���̕ύX��A�uInfo�v�y�[�W�̉����ɕ\�������usave changes�i�ύX��ۑ��j�v�{�^�����N���b�N���邱�Ƃ����Y��Ȃ��B

![�@��̏ڍ�](/guides/devices/netcomm/info.png)

## <a name="network"></a>�l�b�g���[�N�p�����[�^�[�̍\���ݒ�

���L�̃X�N���[���V���b�g�Ɏ�����Ă���ʂ�A�uNetwork�i�l�b�g���[�N�j�v�^�u���ŁA�s���ȃ��o�C���l�b�g���[�N�iWAN�j�⃍�[�J���G���A�l�b�g���[�N�iLAN�j�̉{������э\���ݒ���s�����Ƃ��ł��܂��B

���[�U�[�C���^�[�t�F�[�X�ɕ\������郂�o�C���l�b�g���[�N�iWAN�j�p�����[�^�[�́A���[�^�[�ɕۑ������ŏ��̃v���t�@�C���ɑ������܂��B�����̃p�����[�^�[�̍\���ݒ�����u����ɂ��A���ڂ܂���SMS�o�R�ōs�����Ƃ��ł��܂��B

SMS�\���ݒ�̏ꍇ�ASMS�R�}���h���󂯕t����悤���[�^�[���\���ݒ肷��K�v������܂��BSMS�\���ݒ�̊֘A�p�����[�^�[�Ɋւ��郋�[�^�[�̃}�j���A�������������������A�܂��̓��[�^�[�̃E�F�u���[�U�[�C���^�[�t�F�[�X�����g�p���������B�܂��A�����̃A�J�E���g��SMS�Q�[�g�E�F�C���\���ݒ肷��K�v������܂��B SMS�Q�[�g�E�F�C�̃Z�b�g�A�b�v�ɂ��Ă� [�T�|�[�g�S��](https://support.cumulocity.com) �ւ��₢���킹���������B Device Shell�ɂ��ďڂ����� [���[�U�[�K�C�h](https://cumulocity.com/guides/users-guide/device-management/#shell)���������������B

> ���L�FIP��SMS���[�h�̗��������WAN�p�����[�^�[���\���ݒ肷��ɂ́ACumulocity 7.26���K�v�ł��BAPN�\���ݒ�����ƁA�@�킪���o�C���l�b�g���[�N�ڑ��������A����ꂽSMS�@�\�ɂ��Ǘ������ł��Ȃ��Ȃ�܂��B

![WAN�p�����[�^�[](/guides/devices/netcomm/wan.png)

LAN�����DHCP�̃p�����[�^�[�̍\���ݒ���ACumulocity���璼�ڍs�����Ƃ��ł��܂��B

![LAN�p�����[�^�[](/guides/devices/netcomm/lan.png)

## <a name="software"></a>�\�t�g�E�F�A����уt�@�[���E�F�A�̊Ǘ�

���[�^�[�ɃC���X�g�[���ς݂̃\�t�g�E�F�A����уt�@�[���E�F�A�̉��u�Ǘ����A[�@��Ǘ����[�U�[�K�C�h](/guides/users-guide/device-management#software-repo)�ɋL�ڂ̒ʂ�ACumulocity���񋟂���W���̃\�t�g�E�F�A�^�t�@�[���E�F�A�Ǘ��@�\���g�p���čs�����Ƃ��ł��܂��B

�\�t�g�E�F�A�p�b�P�[�W�� [ipkg](http://en.wikipedia.org/wiki/Ipkg) �`���ł���K�v������A�܂��u&lt;�p�b�P�[�W��&gt;\_&lt;�o�[�W����&gt;\_&lt;arch&gt;.ipk�v�Ƃ��������@�ɏ]���K�v������܂��B�������܂ރo�[�W�����ԍ��̓T�|�[�g����܂���B�p�b�P�[�W�Ǘ����@�i�C���X�g�[���A�A�b�v�O���[�h�A�_�E���O���[�h�A�폜�j�͂��ׂāA���[�^�[�̃p�b�P�[�W�}�l�[�W���[�o�R�ŃT�|�[�g����܂��B�\�t�g�E�F�A�p�b�P�[�W�ɏ]����������ꍇ�A�K���������ɃC���X�g�[�����Ă��������B

> �usmartrest-agent\_&lt;version&gt;\_arm.ipk�v�Ƃ����p�b�P�[�W�́ANetComm�G�[�W�F���g�ł��邱�Ƃ��Ӗ����܂��B���̃p�b�P�[�W��Cumulocity����폜���Ă͂Ȃ�܂���B

> 2.1.1���Â��o�[�W��������A�b�v�O���[�h����ꍇ�A�@��̍ēo�^���K�v�ł��B

�t�@�[���E�F�A�����[�^�[��ɃA�b�v���[�h����уC���X�g�[�����邱�Ƃ��ł��܂��B�t�@�[���E�F�A�𐳏�ɃA�b�v�O���[�h���邽�߁A�Ώۃt�@�[���E�F�A�ɃG�[�W�F���g�p�b�P�[�W���܂܂�邱�Ƃ��m�F���Ă��������B�G�[�W�F���g�p�b�P�[�W���Ώۃt�@�[���E�F�A�Ɋ܂܂�Ă��Ȃ��ƁA�C���X�g�[����ɃG�[�W�F���g���N�����܂���B �t�@�[���E�F�A�t�@�C����Netcomm�̖����@�i�u&lt;name&gt;\_&lt;version&gt;.cdi�v�j�ɏ]���K�v������܂��B

![�\�t�g�E�F�A�^�t�@�[���E�F�A](/guides/devices/netcomm/software.png)

## <a name="system"></a>�V�X�e�����\�[�X�̊Ď�

���[�^�[�̃V�X�e�����\�[�X�g�p�󋵂Ɋւ��铝�v���L�^���āA�g���u���V���[�e�B���O�ɖ𗧂Ă邱�Ƃ��ł��܂��B�ȉ��̓��v���擾���邱�Ƃ��ł��܂��B

* CPU���ׁi�P�ʁF�p�[�Z���g�j
* �������g�p������ё��������e�ʁi�P�ʁFMB�j
* ���ׂẴC���^�[�t�F�[�X��ł̃A�b�v�����N����у_�E�������N�̃g���t�B�b�N�i�P�ʁFKB/�b�j

�����ݒ�ł́A���\�[�X���v���W�������ƂȂ��Ă��܂��B�L��������ꍇ�A [���[�^�[���[�U�[�C���^�[�t�F�[�X](#configure) �ɂ�����uSystem resources measurements�i�V�X�e�����\�[�X����j�v�̎��W�Ԋu���[���ɐݒ肷�邩�A�܂��� [Device Shell](#shell)���g�p���Ĉȉ��̒ʂ�ݒ肵�܂��B

	set service.cumulocity.plugin.system_resources.interval=<interval>

�uMeasurements�i���茋�ʁj�v�^�u�܂��̓_�b�V���{�[�h����A���W���ꂽ�f�[�^�ɃA�N�Z�X���邱�Ƃ��ł��܂��B

## <a name="gps"></a>GPS�̎g�p

���[�^�[�̏��ݓ���܂��͒ǐՂ��s���ꍇ�AGPS�A���e�i�����[�^�[�֐ڑ����A���[�^�[��GPS�@�\��L�������܂��B�����ŁuGPS position interval�iGPS�ʒu�Ԋu�j�v����с^�܂��́uGPS position event�iGPS�ʒu�C�x���g�j�v�̒l���[���ɐݒ肷�邱�Ƃɂ��A�f�[�^���W�p�x�� [�ݒ�](#configure) ���܂��B�uGPS position interval�v�́A���[�^�[�̌��݈ʒu�̍X�V�p�x���`���܂��B�uGPS position event"�v�́A�ǐՂ�ړI�Ɍ��݈ʒu���ʒu�X�V�C�x���g�Ƃ��ĕۑ�����p�x���`���܂��B���l�ɁA�����̃p�����[�^�[���ȉ��̒ʂ�Device Shell����ݒ肷�邱�Ƃ��ł��܂��B

	set service.cumulocity.plugin.ntc6200.gps.update_interval=<update interval>
	set service.cumulocity.plugin.ntc6200.gps.interval=<event interval>

�\���ݒ�ύX��K�p������A�ŏ���GPS�f�[�^�̓����܂ł��΂炭�҂��Ă���A�y�[�W���ēǂݍ��݂��܂��B��������ƁA�uLocation�i�ʒu�j�v�^�u�ƁuTracking�i�ǐՁj�v�^�u���\�������͂��ł��B�ڂ����̓��[�U�[�K�C�h�́u[Location](/guides/users-guide/device-management#location)�v����сu[Tracking](/guides/users-guide/device-management#tracking)�v�̃Z�N�V�������������������B

## <a name="gpio"></a>GPIO�̎g�p

�ȉ���GPIO�@�\���T�|�[�g����Ă��܂��B

* �A�i���O���͂̓d���𑪒茋�ʂƂ���Cumulocity�֑��M����B
* �f�W�^�����͂�1�ɂȂ�ƃA���[�����N�����A0�ɂȂ�ƃA���[������������B
* �f�W�^���o�͂�Cumulocity���牓�u����ŏ������ށB

�ʂ�IO�ݒ�ɂ��ďڂ����́A�����g�̃��[�^�[�̊֘A�������������������B���p�\�ȋ@�\�́A�@��̌^���ɂ���ĈقȂ�ꍇ������܂��B �Ⴆ�΁ANTC 6200�^��GPIO�s��1�`3�ɑΉ��������ANTC 140W�^��GPIO�s��1�ɂ����Ή����܂���B

### �A�i���O����

GPIO�s���̓��͓d���̃|�[�����O�����I�ɍs���A���ʂ�Cumulocity���M�������ꍇ�A�u[GPIO analog measurements�iGPIO�A�i���O����j](#configure)�v�̒l���[���ɐݒ肵�܂��B���邢�͈ȉ��̒ʂ�ADevice Shell���g�p���܂��B

	set service.cumulocity.plugin.ntc6200.gpio.interval=<interval>
	set service.cumulocity.gpio.<port>.notify=measurement

&lt;port&gt;��GPIO�s���̕t�Ԃ��w���܂��BNTC-6200�̏ꍇ�A&lt;port&gt;�̒l��1�A2�܂���3�̂����ꂩ�ł������ANTC-140W�̏ꍇ�A&lt;port&gt;�̒l��1�݂̂ł��B���ʂ͉�������āuMeasurements�i���茋�ʁj�v�ɕ\������܂��B

### �f�W�^������

�f�W�^�����͂���A���[�����N�����邱�Ƃ��ł��܂��B�����̍\���ݒ���A���[�^�[���[�U�[�C���^�[�t�F�[�X���g�p���邩�A�܂���Device Shell�o�R�ōs�����Ƃ��ł��܂��B �`���͈ȉ��̒ʂ�ł��B

	set service.cumulocity.gpio.<port>.notify=alarm
	set service.cumulocity.gpio.<port>.debounce.interval=<SECONDS>
	set service.cumulocity.gpio.<port>.alarm.text=<ALARM_TEXT>
	set service.cumulocity.gpio.<port>.alarm.severity=<severity>

�ʒm�p�����[�^�[�Ƃ��ĉ\�Ȓl�͈ȉ��̒ʂ�ł��B

* off�F�s���͂�����ʒm�ɂ��Ė����ł��B
* alarm�F�s���̓ǂݎ��l���uhigh�i���j�v�̏ꍇ�ɃA���[�����N������܂��B
* measurement�F�d���̃A�i���O�ǂݎ��l�����茋�ʂƂ��đ��M����܂��B

�f�o�E���X�Ԋu��GPIO���͂���̓d�C�m�C�Y��ጸ���܂��B �܂�A�Ԋu���Z���قǒl�̃m�C�Y���傫���Ȃ�܂����A�M���̕ω��ɑ΂��锽���͑����Ȃ�܂��B�����ݒ�̃f�o�E���X�Ԋu��10���Ԃł��B

�utext�i�e�L�X�g�j�v�v���p�e�B�̐ݒ�ɂ��A�����ݒ�̃A���[���e�L�X�g�𖳌��ɂ��邱�Ƃ��ł��܂��B�����ݒ�ł͂��̒l����̏�ԂŁA�ugpio&lt;N&gt; is active�igpio&lt;N&gt;���L���ł��j�v���e�L�X�g�Ƃ��Ďg�p���܂��B&lt;N&gt;��GPIO�s���̕t�Ԃ��w���܂��B

�L���ȃA���[���d��x�͈ȉ��̒ʂ�ł��B

 * WARNING�i�x���j
 * MINOR�i�y���j
 * MAJOR�i�d��j [�����ݒ�]
 * CRITICAL�i�ɂ߂ďd��j

���͂�1�b���ɁA�ω����Ȃ����m�F����܂��B

### �f�W�^���o��

�uRelay array�i�����[�z��j�v�v���O�C�����g�p���āA�f�W�^���o�͂𐧌䂷�邱�Ƃ��ł��܂��B���L�̃X�N���[���V���b�g���������������BGPIO�s���̕t�Ԃ̓��[�^�[�ł̕t�ԂƓ����ł��BNTC-6200�^�̏ꍇ�A3�ʂ��GPIO�s����ݒ�ł��܂����ANTC-140W�^�ł͍ŏ��̃s���̂ݗL���ł��B

![Relay Array](/guides/devices/netcomm/relayarray.png)

## <a name="rdb"></a>�\���ݒ�Ǘ�

���[�U�[�\���ݒ�f�[�^�������A�C������ѕۑ����邱�Ƃ��ł��܂��B To do ���s����ꍇ�A���[�^�[�́u[Configuration�i�\���ݒ�j](/guides/users-guide/device-management#operation-monitoring)�v�^�u�փi�r�Q�[�g���A�uCONFIGURATION�v�E�B�W�F�b�g���́uReload�i�����[�h�j�v�{�^�����N���b�N���č\���ݒ�f�[�^�����N�G�X�g���܂��B�_�E�����[�h�ɐ��b������܂��B�\���ݒ�f�[�^����������ƁA�p�����[�^�[�ꗗ�Ɗe�p�����[�^�[�ɑΉ�����l���\������܂��B���̌�A�\���ݒ�ɕύX�������A�@��ɖ߂��`�ŕۑ����邱�Ƃ��ł��܂��B

�\���ݒ�̃X�i�b�v�V���b�g���@��Ƀ��N�G�X�g���A�������ő��̋@��ɓK�p���邱�Ƃ��ł��܂��B

�G�[�W�F���g�̃o�[�W����3.11�����Cumulocity�̃o�[�W����7.26�ȍ~�ARDB�X�i�b�v�V���b�g�ɂ��Ή�����悤�ɂȂ��Ă��܂����A����͍\���ݒ�̂���Ώ�ʏW���ł��B����͎�Ƀg���u���V���[�e�B���O���ړI�ł��B

![RDB�Z�b�g�A�b�v](/guides/devices/netcomm/rdb.png)

> Cumulocity 6.9���O�܂ŁA���̃E�B�W�F�b�g�́uControl�i����j�v�^�u�Ɋ܂܂�Ă��܂����B Cumulocity 6.9�ȍ~�A�@��̔�e�L�X�g�������܂߂��\���ݒ�S�̂̃X�i�b�v�V���b�g���擾������A�\���ݒ�̎Q�ƃX�i�b�v�V���b�g���@��ɑ���Ԃ����肷�邱�Ƃ��ł��܂��B

## <a name="sms_mode"></a> �@���SMS���[�h�Ŏg�p����ꍇ�̍\���ݒ�

�@�������SMS�R�}���h���g�p����ꍇ�A���[�^�[�̃E�F�u�C���^�[�t�F�[�X���J���A�uServices�i�T�[�r�X�j�v����uSMS messaging�iSMS���b�Z�[�W���O�j�v�A�����āuDiagnostics�i�f�f�j�v�ւƃi�r�Q�[�g���܂��B�@��̍\���ݒ�菇�͈ȉ��̒ʂ�ł��B

* �uOnly accept authenticated SMS messages�i�F�؍ς�SMS���b�Z�[�W�̂ݎ󂯕t����j�v�𖳌������邩�A�܂��͋����ꂽ���M�҂��z���C�g���X�g�ɒǉ�����B�p�X���[�h�̎g�p�ɂ͑Ή����Ă��܂���B
* ���̐ݒ���I���ɂ���B

![SMS���[�h�̗L����](/guides/devices/netcomm/sms_mode.png)

> �ڂ����́u[Control devices via SMS�iSMS�o�R�ł̋@��̐���j](/guides/reference/device-control#control_via_sms)�v���������������B

## <a name="shell"></a>Device Shell

Device Shell���g�p���āA�ʂ̍\���ݒ�p�����[�^�[���@�킩��ǂݏ�������ق��A�f�f�p�R�}���h�����s���邱�Ƃ��ł��܂��B �ڂ����� [���[�U�[�K�C�h](/guides/users-guide/device-management#shell)���������������B�L���ȃp�����[�^�[����ѐf�f�p�R�}���h�ɂ��ẮANetcomm�֘A�������������������B��ʓI�Ȍ`���͈ȉ��̒ʂ�ł��B

* �uget &lt;parameter&gt;�v�F�p�����[�^�[���@�킩��ǂݎ��B
* �uset &lt;parameter&gt;=&lt;value&gt;�v�F�p�����[�^�[���@��ɏ������ށB
* �uexecute &lt;command&gt;�v�F�f�f�p�R�}���h���@���Ŏ��s����B

�Z�~�R�������Z�p���[�^�[�Ƃ��Ďg�p����ƁA������get�Aset�����execute�R�}���h�����s�ł��܂��B�g�p�p�x�̍����p�����[�^�[����уR�}���h�ɃA�N�Z�X����ɂ́A�uGet Predefined�i����̂��̂��擾�j�v�����N���N���b�N���܂��B

![Device Shell](/guides/devices/netcomm/shell.png)

## <a name="notifications"></a>�C�x���g�ʒm

���[�^�[�͈��̃V�X�e���C�x���g��ʒm�Ƃ��ĕ񍐂��܂��B�������A���[���Ƃ���Cumulocity�֓]�����邱�Ƃ��ł��܂��B�V�X�e���C�x���g�́A�Ⴆ�΃��o�C���l�b�g���[�N�̖��̃g���u���V���[�e�B���O�ɖ𗧂��܂��B �l�X�Ȏ�ނ̃C�x���g�₻���̓]�����@�ɂ��ďڂ����́ANetcomm�֘A�����i�Ⴆ�΁A���[�U�[�K�C�h�́uEvent notification�i�C�x���g�ʒm�j�v�Z�N�V�����j���������������B�C�x���g���A���[���Ƃ��ē]������ꍇ�A���[�J���z�X�g��̃|�[�g1331���ɑ��M����UDP�����ݒ肵�܂��i�uDestination configuration�i����\���ݒ�j�v�Z�N�V�����Q�Ɓj�B

![�C�x���g�ʒm](/guides/devices/netcomm/notifications.png)

## <a name="modbus"></a>Cloud Fieldbus

Modbus-TCP�����Modbus-RTU�̃X���[�u�ɂ��ꂼ��LAN�o�R����уV���A���|�[�g�o�R�Őڑ����ACumulocity���ŉ��u�Ǘ����邱�Ƃ��ł��܂��B���s����ɂ͈ȉ����s���K�v������܂��B

Modbus-TCP�̐ݒ�菇

* LAN�ڑ����m������B ��L�́u[Network�i�l�b�g���[�N�j](#network)�v�^�u�ƁAModbus�@���̑Ή�����@��\���@�\���g�p���āA���[�^�[�Ǝ�����Modbus-TCP�X���[�u�Ƃ̊Ԃ�IP�ʐM��L��������B
* �����ݒ��502�ƈقȂ�|�[�g���g�p���Ă���ꍇ�ANetComm�@��̃E�F�uUI���Cumulocity���j���[����Modbus-TCP�|�[�g�̍\���ݒ���s���B�u[Configuring the router�i���[�^�[�̍\���ݒ�j](#configure)�v���������������B

Modbus-RTU�̐ݒ�菇

* ���[�^�[�Ǝ�����Modbus-RTU�X���[�u���A�V���A���P�[�u���o�R�Őڑ�����B
* Device Shell�o�R�ňȉ��̒ʂ�V���A���|�[�g�̍\���ݒ���s���B

        set serial.iomode.default=<mode>

`<mode>`��re232�Ars422�܂���rs485�̂����ꂩ�Ƃ��邱�Ƃ��ł��܂��B���[�h�ύX��A�@��̍ċN�����K�v�ƂȂ�ꍇ������܂��B

> �����ݒ�̃V���A���|�[�g`/dev/ttyAPP4`�́A�ǉ��̍\���ݒ���s��Ȃ��Ă��@�\����͂��ł��B��̏ꍇ�A�܂��͕ʂ̃|�[�g���\���ݒ肷��K�v������ꍇ�A�@��̃E�F�uUI����Cumulocity���j���[�ō\���ݒ�\�ł��B�u[Configuring the router�i���[�^�[�̍\���ݒ�j](#configure)�v���������������B

> USB�^�V���A���ϊ����u�̒��ɂ́A�G�R�[���[�h�������ݒ�ŗL���ƂȂ��Ă�����̂�����A�����Modbus�ʐM�̍쓮�����S�Ɏ~�߂Ă��܂��\��������܂��B���̂悤�ȕϊ����u���������̏ꍇ�A�������菇�ɂ��ă��[�J�[�ɂ��₢���킹���������B

> NTC-140W�^��Modbus RTU�ɑΉ����Ă��܂���̂ŁA�Y������@�\��UI�ɕ\������܂���B


���̏ꍇ�̎菇�͈ȉ��̒ʂ�ł��B

* [�T�|�[�g�S��](https://support.cumulocity.com)�ɘA�����A�����̃A�J�E���g��Cloud Fieldbus�A�v���P�[�V�����Ŏg����悤�ɂ��Ă��炤�B
* [Cloud Fieldbus�̃��[�U�[�K�C�h](/guides/users-guide/cloud-fieldbus)�ɋL�ڂ̒ʂ�AModbus�ʐM�̍\���ݒ���s���B
* �@��̃E�F�uUI����Cumulocity���j���[�ŁuModbus read only�iModbus�ǂݎ���p�j�v�v���p�e�B��ݒ肷�邱�Ƃɂ��A�������݃p�[�~�b�V������L�����܂��͖���������B�u[Configuring the router�i���[�^�[�̍\���ݒ�j](#configure)�v���������������B 0�ɐݒ肷��Ə������݃p�[�~�b�V�����̋����Ӗ����A1��Modbus�̏������݃p�[�~�b�V�����������Ȃ����Ƃ��Ӗ����܂��B

## <a name="logs"></a>���O�r���A�[

�e�탍�O���@�킩��_�E�����[�h���A�{�����邱�Ƃ��ł��܂��B���O�t�@�C���͂��Ȃ�傫���ꍇ������܂��̂ŁA�֐S�̂�����e�̂݉{�������ꍇ�A�t�B���^�����O���ݒ肷�邱�Ƃ��ł��܂��B

�E���œ��t�͈͂�ݒ肵�A���O�t�@�C����I�����邱�Ƃ��ł��܂��B���ɁA�e�L�X�g���������A���v����s�̂݋@�킩��擾���邱�Ƃ��ł��܂����v����s�𐧌����邱�Ƃ��ł��܂��B

�擾���ꂽ���O�����̃��X�g�ɕ\������܂��B�N���b�N����ƃ��O�t�@�C���̓��e���y�[�W�̉����ɕ\������܂��B�O�񃊃N�G�X�g�������O�������I�ɊJ����܂��B

![���O�r���A�[](/guides/devices/netcomm/logs.png)
