package abdom.math.stats;


/****************************/
/* �����z�̌v�Z             */
/*      coded by Y.Suganuma */
/****************************/
import java.io.*;
import java.util.*;

/**
 * t���z�̗ݐϕ��z�֐��ł��B
 */
public class Tdist {

//	static double p;   // �����l���v�Z����Ƃ�����/100��ݒ�
//	static int dof;    // ���R�x(degree of freedom)
	
	/**
	 * �ݐϕ��z�֐�(cumulative distribution function)
	 */
	public static double dist(int degreeOfFreedom, double percentile) {
		int[] ind = new int[1];
		double result = App.p_t(degreeOfFreedom, 0.01d*percentile, ind);
		if (ind[0] == -1) throw new RuntimeException("t���z�̗ݐϕ��z�֐����^����ꂽ�����Ŏ������܂���ł����B�p�[�Z���^�C��="+percentile+"  ���R�x="+degreeOfFreedom);
		return result;
	}
	
	/**
	 * �e�X�g�p���C��
	 * Smirnov-Grubbs����� n=8, alpha=0.05 �Ƃ����ꍇ�� t �̒l
	 *
	 * t = ���R�xn-2 ��t���z�� alpha/n*100 �p�[�Z���^�C��
	 */
	public static void main(String[] args) throws Exception {
		int n = 1000;
		double alpha = 0.01; // �Б�
		
		double t = Tdist.dist(n-2, alpha/n*100);
		System.out.println("t = " + t);
        double gamma = ((n-1) * t)/Math.sqrt(n * (n-2) + n * t * t);
		System.out.println("gamma = " + gamma);
	}
	
}

/****************/
/* �֐��l�̌v�Z */
/****************/
class Kansu {
	private int sw;
					// �R���X�g���N�^
	Kansu (int s) {sw = s;}
					// double�^�֐�
	double snx(double x, int dof, double percentile)
	{

		double y = 0.0, w[] = new double [1];

		switch (sw) {
						// �֐��l�if(x)�j�̌v�Z�i���K���z�j
			case 0:
				y = 1.0 - percentile - App.normal(x, w);
				break;
						// �֐��l�if(x)�j�̌v�Z�i�����z�j
			case 1:
				y = App.t(x, w, dof) - 1.0 + percentile;
				break;
						// �֐��̔����̌v�Z�i�����z�j
			case 2:
				y = App.t(x, w, dof);
				y = w[0];
				break;
		}

		return y;
	}
}

/************************/
/* �Ȋw�Z�p�n�Z�p�̎�@ */
/************************/
class App {

	/**************************************************/
	/* �����z�̌v�Z�iP(X = tt), P(X < tt)�j           */
	/* �i���R�x�����̎��̒l��N(0,1)�𗘗p���ĉ������j */
	/*      dd : P(X = tt)                            */
	/*      df : ���R�x                               */
	/*      return : P(X < tt)                        */
	/**************************************************/
	static double t(double tt, double dd[], int df)
	{
		double pi = Math.PI;
		double p, pp, sign, t2, u, x;
		int ia, i1;

		sign = (tt < 0.0) ? -1.0 : 1.0;
		if (Math.abs(tt) < 1.0e-10)
			tt = sign * 1.0e-10;
		t2 = tt * tt;
		x  = t2 / (t2 + df);

		if(df%2 != 0) {
			u  = Math.sqrt(x*(1.0-x)) / pi;
			p  = 1.0 - 2.0 * Math.atan2(Math.sqrt(1.0-x), Math.sqrt(x)) / pi;
			ia = 1;
		}

		else {
			u  = Math.sqrt(x) * (1.0 - x) / 2.0;
			p  = Math.sqrt(x);
			ia = 2;
		}

		if (ia != df) {
			for (i1 = ia; i1 <= df-2; i1 += 2) {
				p += 2.0 * u / i1;
				u *= (1.0 + i1) / i1 * (1.0 - x);
			}
		}

		dd[0] = u / Math.abs(tt);
		pp  = 0.5 + 0.5 * sign * p;

		return pp;
	}

	/**************************************************/
	/* �����z�̂����l�iP(X > u) = 0.01p�j             */
	/* �i���R�x�����̎��̒l��N(0,1)�𗘗p���ĉ������j */
	/*      ind : >= 0 : normal�i�����񐔁j           */
	/*            = -1 : �������Ȃ�����               */
	/**************************************************/
	static double p_t(int dof, double percentile, int ind[])
	{
		double pi = Math.PI;
		double c, df, df2, e, pis, p2, tt = 0.0, t0, x, yq;

		pis = Math.sqrt(pi);
		df  = (double)dof;
		df2 = 0.5 * df;
					// ���R�x���P
		if (dof == 1)
			tt = Math.tan(pi*(0.5-percentile));

		else {
					// ���R�x���Q
			if (dof == 2) {
				c   = (percentile > 0.5) ? -1.0 : 1.0;
				p2  = (1.0 - 2.0 * percentile);
				p2 *= p2;
				tt  = c * Math.sqrt(2.0 * p2 / (1.0 - p2));
			}
					// ���R�x���Q
			else {

				yq = p_normal(ind, dof, percentile);   // �����l�v�Z�̂���

				if (ind[0] >= 0) {

					x  = 1.0 - 1.0 / (4.0 * df);
					e  = x * x - yq * yq / (2.0 * df);

					if (e > 0.5)
						t0 = yq / Math.sqrt(e);
					else {
						x  = Math.sqrt(df) / (pis * percentile * df * gamma(df2, ind) / gamma(df2+0.5, ind));
						t0 = Math.pow(x, 1.0/df);
					}
						// �j���[�g���@
					Kansu kn1 = new Kansu(1);
					Kansu kn2 = new Kansu(2);

					tt = newton(t0, 1.0e-6, 1.0e-10, 100, ind, kn1, kn2, dof, percentile);
				}
			}
		}

		return tt;
	}

	/****************************************/
	/* ���i���j�̌v�Z�i�K���}�֐��C�ߎ����j */
	/*      ier : =0 : normal               */
	/*            =-1 : x=-n (n=0,1,2,���)  */
	/*      return : ����                   */
	/****************************************/
	static double gamma(double x, int ier[])
	{
		double err, g, s, t, v, w, y;
		int k;

		ier[0] = 0;

		if (x > 5.0) {
			v = 1.0 / x;
			s = ((((((-0.000592166437354 * v + 0.0000697281375837) * v +
                0.00078403922172) * v - 0.000229472093621) * v -
                0.00268132716049) * v + 0.00347222222222) * v +
                0.0833333333333) * v + 1.0;
			g = 2.506628274631001 * Math.exp(-x) * Math.pow(x,x-0.5) * s;
		}

		else {

			err = 1.0e-20;
			w   = x;
			t   = 1.0;

			if (x < 1.5) {

				if (x < err) {
					k = (int)x;
					y = (double)k - x;
					if (Math.abs(y) < err || Math.abs(1.0-y) < err)
						ier[0] = -1;
				}

				if (ier[0] == 0) {
					while (w < 1.5) {
						t /= w;
						w += 1.0;
					}
				}
			}

			else {
				if (w > 2.5) {
					while (w > 2.5) {
						w -= 1.0;
						t *= w;
					}
				}
			}

			w -= 2.0;
			g  = (((((((0.0021385778 * w - 0.0034961289) * w + 
                 0.0122995771) * w - 0.00012513767) * w + 0.0740648982) * w +
                 0.0815652323) * w + 0.411849671) * w + 0.422784604) * w +
                 0.999999926;
			g *= t;
		}

		return g;
	}

	/*************************************************/
	/* �W�����K���zN(0,1)�̌v�Z�iP(X = x), P(X < x)�j*/
	/*      w : P(X = x)                             */
	/*      return : P(X < x)                        */
	/*************************************************/
	static double normal(double x, double w[])
	{
		double y, z, P;
	/*
	     �m�����x�֐��i��`���j
	*/
		w[0] = Math.exp(-0.5 * x * x) / Math.sqrt(2.0*Math.PI);
	/*
	     �m�����z�֐��i�ߎ������g�p�j
	*/
		y = 0.70710678118654 * Math.abs(x);
		z = 1.0 + y * (0.0705230784 + y * (0.0422820123 +
            y * (0.0092705272 + y * (0.0001520143 + y * (0.0002765672 +
            y * 0.0000430638)))));
		P = 1.0 - Math.pow(z, -16.0);

		if (x < 0.0)
			P = 0.5 - 0.5 * P;
		else
			P = 0.5 + 0.5 * P;

		return P;
	}

	/******************************************************************/
	/* �W�����K���zN(0,1)�̂����l�iP(X > u) = 0.01p�j�i�񕪖@���g�p�j */
	/*      ind : >= 0 : normal�i�����񐔁j                           */
	/*            = -1 : �������Ȃ�����                               */
	/******************************************************************/
	static double p_normal(int ind[], int dof, double percentile)
	{
		double u;
		int sw[] = new int [1];

		Kansu kn = new Kansu(0);

		u        = bisection(-7.0, 7.0, 1.0e-6, 1.0e-10, 100, sw, kn, dof, percentile);
		ind[0]   = sw[0];

		return u;
	}

	/*****************************************************/
	/* Newton�@�ɂ�����`������(f(x)=0)�̉�            */
	/*      x1 : �����l                                  */
	/*      eps1 : �I�������P�i�bx(k+1)-x(k)�b��eps1�j   */
	/*      eps2 : �I�������Q�i�bf(x(k))�b��eps2�j       */
	/*      max : �ő厎�s��                           */
	/*      ind : ���ۂ̎��s��                         */
	/*            �i���̎��͉��𓾂邱�Ƃ��ł��Ȃ������j */
	/*      kn1 : �֐����v�Z����N���X�I�u�W�F�N�g       */
	/*      kn2 : �֐��̔������v�Z����N���X�I�u�W�F�N�g */
	/*      return : ��                                  */
	/*****************************************************/
	static double newton(double x1, double eps1, double eps2, int max,
                         int ind[], Kansu kn1, Kansu kn2, int dof, double percentile)
	{
		double g, dg, x;
		int sw;

		x      = x1;
		ind[0] = 0;
		sw     = 0;

		while (sw == 0 && ind[0] >= 0) {

			ind[0]++;
			sw = 1;
			g  = kn1.snx(x1, dof, percentile);

			if (Math.abs(g) > eps2) {
				if (ind[0] <= max) {
					dg = kn2.snx(x1, dof, percentile);
					if (Math.abs(dg) > eps2) {
						x = x1 - g / dg;
						if (Math.abs(x-x1) > eps1 && Math.abs(x-x1) > eps1*Math.abs(x)) {
							x1 = x;
							sw = 0;
						}
					}
					else
						ind[0] = -1;
				}
				else
					ind[0] = -1;
			}
		}

		return x;
	}

	/*********************************************************/
	/* �񕪖@�ɂ�����`������(f(x)=0)�̉�                  */
	/*      x1,x2 : �����l                                   */
	/*      eps1 : �I�������P�i�bx(k+1)-x(k)�b��eps1�j       */
	/*      eps2 : �I�������Q�i�bf(x(k))�b��eps2�j           */
	/*      max : �ő厎�s��                               */
	/*      ind : ���ۂ̎��s��                             */
	/*            �i���̎��͉��𓾂邱�Ƃ��ł��Ȃ������j     */
	/*      kn : �֐��l���v�Z����N���X�I�u�W�F�N�g          */
	/*      return : ��                                      */
	/*********************************************************/
	static double bisection(double x1, double x2, double eps1, double eps2, int max, int ind[], Kansu kn, int dof, double percentile)
	{
		double f0, f1, f2, x0 = 0.0;
		int sw;

		f1 = kn.snx(x1, dof, percentile);
		f2 = kn.snx(x2, dof, percentile);

		if (f1*f2 > 0.0)
			ind[0] = -1;

		else {
			ind[0] = 0;
			if (f1*f2 == 0.0)
				x0 = (f1 == 0.0) ? x1 : x2;
			else {
				sw = 0;
				while (sw == 0 && ind[0] >= 0) {
					sw      = 1;
					ind[0] += 1;
					x0      = 0.5 * (x1 + x2);
					f0      = kn.snx(x0, dof, percentile);
					if (Math.abs(f0) > eps2) {
						if (ind[0] <= max) {
							if (Math.abs(x1-x2) > eps1 && Math.abs(x1-x2) > eps1*Math.abs(x2)) {
								sw = 0;
								if (f0*f1 < 0.0) {
									x2 = x0;
									f2 = f0;
								}
								else {
									x1 = x0;
									f1 = f0;
								}
							}
						}
						else
							ind[0] = -1;
					}
				}
			}
		}

		return x0;
	}
}

