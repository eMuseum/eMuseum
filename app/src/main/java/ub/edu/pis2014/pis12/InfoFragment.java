package ub.edu.pis2014.pis12;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.espian.showcaseview.ShowcaseViews;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.animation.AnimatorProxy;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import ub.edu.pis2014.pis12.controlador.API;
import ub.edu.pis2014.pis12.controlador.APINotifier;
import ub.edu.pis2014.pis12.controlador.APIOperation.TASK_TYPE;
import ub.edu.pis2014.pis12.controlador.GridAdapterComentaris;
import ub.edu.pis2014.pis12.controlador.ImageController;
import ub.edu.pis2014.pis12.controlador.ScreenSlidePagerAdapter;
import ub.edu.pis2014.pis12.model.Autor;
import ub.edu.pis2014.pis12.model.Comentari;
import ub.edu.pis2014.pis12.model.Dades;
import ub.edu.pis2014.pis12.model.Element;
import ub.edu.pis2014.pis12.model.Museu;
import ub.edu.pis2014.pis12.model.Obra;
import ub.edu.pis2014.pis12.model.TIPUS_ELEMENT;
import ub.edu.pis2014.pis12.model.Usuari;
import ub.edu.pis2014.pis12.utils.AlphaBoth;
import ub.edu.pis2014.pis12.utils.AlphaToggle;
import ub.edu.pis2014.pis12.utils.AnimationNotifier;
import ub.edu.pis2014.pis12.utils.AnimationToggle;
import ub.edu.pis2014.pis12.utils.ElementImageManager;
import ub.edu.pis2014.pis12.utils.ExpandableHeightGridView;
import ub.edu.pis2014.pis12.utils.HeightToggle;
import ub.edu.pis2014.pis12.utils.ScrollLoader;
import ub.edu.pis2014.pis12.utils.ScrollLoaderListener;
import ub.edu.pis2014.pis12.utils.ScrollViewEx;
import ub.edu.pis2014.pis12.utils.Utils;

@SuppressLint({"FloatMath", "InlinedApi"})
public class InfoFragment extends Fragment implements TextToSpeech.OnInitListener {
    public static final int ACTIVITY_CREATE = 1;
    //Valors de la imatge en miniatura
    public static float startScale = 0;
    public static Rect startBounds = new Rect();

    //Actvity i el view del fragment
    private Context context;
    private FragmentActivity activity;
    private MediaPlayer mPlayer = null;
    private String destFilename = "";
    private boolean created = false;

    /*
     * Eliminem els warnings ja que l'objectiu d'aquestes variables es que el GC
     * (Garbage Collector) de Java no elimini els efectes
     */
    private AlphaToggle informacioToggle = null;
    private HeightToggle informacioHeightToggle = null;
    private AlphaToggle opinionsToggle = null;
    private HeightToggle opinionsHeightToggle = null;

    private static boolean informacioToggled = false;
    private static boolean opinionsToggled = false;
    private boolean createAnimations = true;

    private boolean registratToast = false;
    private static boolean expandActivat = false;

    private Calendar cal = Calendar.getInstance();
    //Per pasar a la nova activity en el intent

    // Cal fer estatica la variable que indica si podem o no
    // Fer servir aquest fragment en el canvi d'horitzontal a vertical (si es
    // una obra)
    private static boolean orientationSwitch = false;

    // Indica si estem reproduint l'audio o no
    private boolean reproduint = false;
    private boolean stopped = true;
    private TextToSpeech tts;

    // Declaracio i inicialitacio dels identificadors pasats en l'intent
    // anterior
    private int id_element;
    private TIPUS_ELEMENT tipus;

    // Elements
    private Element element = null;
    private RatingBar barraValoracions = null;
    private ExpandableHeightGridView gridviewComentaris = null;
    private GridAdapterComentaris adapter = null;
    private RelativeLayout loadMore = null;
    private View loadMoreImg = null;
    private View upForMore = null;
    private View upClick = null;
    private TextView textInformacio = null;
    private EditText editorComentari = null;
    private ImageView reproPlay = null;
    private ImageView reproStop = null;
    private TextView txtDireccio = null;
    private TouchImageView expandedImageView = null;


    /**
     * Hold a reference to the current animator, so that it can be canceled mid-way.
     */
    private static Animator mCurrentAnimator;

    /**
     * The system "short" animation time duration, in milliseconds. This duration is ideal for
     * subtle animations or animations that occur very frequently.
     */
    private static int mShortAnimationDuration;

    /**
     * Aquest metode permet crear un nou fragment i passar-l'hi parametres a
     * partir d'un element
     *
     * @param element Element del que agafar parametres
     * @return Retorna el nou fragment
     */
    public static InfoFragment newInstance(Element element) {
        // Creem uns nous arguments
        Bundle args = new Bundle();
        args.putBoolean("createAnimations", true);
        args.putInt("id", element.getId());
        args.putSerializable("tipus", TIPUS_ELEMENT.getFromElement(element));

        // Creem el fragment i el retornem
        InfoFragment fragment = new InfoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Permet crear un nou fragment passant-l'hi un Bundle, per exemple, dels
     * extras d'un intent
     *
     * @param args Bundle amb parametres
     * @return Retorna el nou fragment
     */
    public static InfoFragment newInstance(Bundle args, boolean anim) {
        args.putBoolean("createAnimations", anim);

        InfoFragment fragment = new InfoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Al crear unicament inflem el container amb el layout
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info, container, false);
    }

    /**
     * Al estar ja creada, carreguem i configurem cada un dels elements
     */
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        // Variables globals
        activity = getActivity();
        context = view.getContext();
        tts = new TextToSpeech(context, null);

        // Inicialitzem elements globals

        // Obtenim i declarem el gridView dels comentaris
        gridviewComentaris = (ExpandableHeightGridView) view.findViewById(R.id.gridView_Comentaris);
        // Declara l'adapter del grid
        adapter = new GridAdapterComentaris(getActivity());

        // Views utilitzades per indicar a l'usuari com carregar mes elements
        final ScrollViewEx scrollInfoObres = (ScrollViewEx) view.findViewById(R.id.scrollInfoObres);
        loadMore = (RelativeLayout) view.findViewById(R.id.load_more_layout);
        loadMoreImg = view.findViewById(R.id.load_more_img);
        upForMore = view.findViewById(R.id.txt_up_more);
        upClick = view.findViewById(R.id.txt_up_click);

        // Informacio
        textInformacio = (TextView) view.findViewById(R.id.text_informacio);

        // Obtenir la barra de valoracions
        barraValoracions = (RatingBar) view.findViewById(R.id.valoracion_ratingBar);

        // Editar omentaris
        editorComentari = (EditText) view.findViewById(R.id.editText_Comentaris);

        // Botons del reproductor de l'element
        reproPlay = (ImageView) view.findViewById(R.id.imageView_play);
        reproStop = (ImageView) view.findViewById(R.id.imageView_stop);

        // Direccio del museu
        txtDireccio = (TextView) view.findViewById(R.id.textView_direccio);

        //Imatge expandida
        expandedImageView = (TouchImageView) activity.findViewById(R.id.expanded_image);

        // Obtenim, si les hem passat, les dades del museu i obra
        Bundle extras = getArguments();
        if (extras != null) {
            id_element = extras.getInt("id");
            tipus = (TIPUS_ELEMENT) extras.getSerializable("tipus");
            createAnimations = extras.getBoolean("createAnimations");
        }

        orientationSwitch = tipus == TIPUS_ELEMENT.TIPUS_OBRA;

        // Obtenim el titol de l'obra, museu o autor i el mostrem segons el
        // tipus que sigui
        final TextView txtTelefon = (TextView) view.findViewById(R.id.textView_telefon);
        final TextView txtHorari = (TextView) view.findViewById(R.id.textView_horari);
        final TextView txtMuseu = (TextView) view.findViewById(R.id.textView_museu);
        final TextView txtAutor = (TextView) view.findViewById(R.id.textView_autor);
        final LinearLayout layoutMuseu = (LinearLayout) view.findViewById(R.id.layout_museu);
        final LinearLayout layoutObra = (LinearLayout) view.findViewById(R.id.layout_obra);

        switch (tipus) {
            case TIPUS_OBRA:
                element = Dades.getObra(id_element);
                layoutMuseu.setVisibility(View.GONE);
                layoutObra.setVisibility(View.VISIBLE);

                Museu museu = Dades.getMuseu(((Obra) element).getMuseuId());
                Autor autor = Dades.getAutor(((Obra) element).getAutorId());
                txtMuseu.setText(museu.getTitol());
                txtAutor.setText(autor.getTitol());
                break;

            case TIPUS_MUSEU:
                element = Dades.getMuseu(id_element);
                layoutMuseu.setVisibility(View.VISIBLE);
                layoutObra.setVisibility(View.GONE);

                Resources resources = getResources();

                String laborables = resources.getString(R.string.fragment_info_laborables) + ": " + ((Museu) element).getHoraObertLaborables() + "h - " + ((Museu) element).getHoraTancatLaborables() + "h ";
                String festius = " " + resources.getString(R.string.fragment_info_festius) + ": " + ((Museu) element).getHoraObertFestius() + "h - " + ((Museu) element).getHoraTancatFestius() + "h";
                String concat = resources.getString(R.string.fragment_info_and);
                txtHorari.setText(laborables + concat + festius);
                txtTelefon.setText(((Museu) element).getTelefon());
                txtDireccio.setText(((Museu) element).getDireccio());

                boolean estat = false;
                int dia_actual = cal.get(Calendar.DAY_OF_WEEK);
                int hora_actual = cal.get(Calendar.HOUR_OF_DAY);
                if ((dia_actual == 1) || (dia_actual == 7)) {
                    if ((hora_actual >= ((Museu) element).getHoraObertFestius()) && (hora_actual <= ((Museu) element).getHoraTancatFestius())) {
                        estat = true;
                    }
                } else {
                    if ((hora_actual >= ((Museu) element).getHoraObertLaborables()) && (hora_actual <= ((Museu) element).getHoraTancatLaborables())) {
                        estat = true;
                    }
                }
                ImageView indicador_estat = (ImageView) view.findViewById(R.id.imageView_estat);
                if (estat) {
                    indicador_estat.setImageResource(android.R.drawable.presence_online);
                } else
                    indicador_estat.setImageResource(android.R.drawable.presence_offline);

                break;

            case TIPUS_AUTOR:
                element = Dades.getAutor(id_element);
                layoutMuseu.setVisibility(View.GONE);
                layoutObra.setVisibility(View.GONE);
                break;

            default:
                element = null;
                // ERROR
                break;
        }

        // Coloquem el titol
        final TextView title = (TextView) view.findViewById(R.id.llistaObresActivity_title);
        title.setText(element.getTitol());

        // Coloquem la valoracio
        barraValoracions.setRating(element.getValoracio());
        barraValoracions.setOnTouchListener(onValoracio);

        // Coloquem la imatge usant el imageDownloader.
        final ImageView imatge = (ImageView) view.findViewById(R.id.imageView_imatge);

        ImageController.getInstance().setImageWithURL(element.getImatgeURL(), imatge);

        //Quan cliquem a la imatge de l'element --> Zoom
        imatge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (element.getImatge() != null) {
                    //TODO: millorar el metode per augmentar una imatge
                    zoomImageFromThumb(imatge, element.getImatge(), view);
                }
            }
        });

        // Posar informacio
        textInformacio.setText(element.getDescripcio());

        // Establim els parametres del gridview
        gridviewComentaris.setScroll(scrollInfoObres);
        gridviewComentaris.setAdapter(adapter);

        //Posem la duracio que volem de l'animacio al augmentar la imatge
        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        // Quan clicquem al boto de comentar
        Button botoComentar = (Button) view.findViewById(R.id.button_comentar);
        botoComentar.setOnClickListener(onComment);

        if (!Usuari.get().isLogged()) {
            botoComentar.setEnabled(false);
            editorComentari.setHint(R.string.fragment_info_register);
            editorComentari.setEnabled(false);
        }

        // Quan cliquem al boto del play/pause i stop
        reproPlay.setOnClickListener(onClickPlay);
        reproPlay.setOnTouchListener(onTouchPlay);
        reproStop.setOnClickListener(onClickStop);
        reproStop.setOnTouchListener(onTouchStop);

        // Quan cliques al text del museu, de l'autor, del telefon o de la direccio
        txtMuseu.setOnClickListener(onClickMuseu);
        txtAutor.setOnClickListener(onClickAutor);
        txtTelefon.setOnClickListener(onClickTelefon);
        txtDireccio.setOnClickListener(onClickDireccio);

        // Obtenim els elements a animar
        final Button mostrarInfo = (Button) view.findViewById(R.id.mostrar_informacio);
        final View informacio = view.findViewById(R.id.contenidor_informacio);
        final Button mostrarOpinions = (Button) view.findViewById(R.id.mostrar_opinions);
        final View opinions = view.findViewById(R.id.contenidor_opinions);


        // Aninem la informacio, fem que aparegui progressivament
        informacioToggle = new AlphaToggle(mostrarInfo, informacio, 1000);
        // Notificador per persistencia en canvis d'orientacio
        informacioToggle.setAnimationNotifier(new AnimationNotifier() {

            @Override
            public void onToggled(AnimationToggle animation, boolean toggled) {
                informacioToggled = toggled;
            }
        });

        // Aninem les opinions, fem que apareguin progressivament
        opinionsToggle = new AlphaToggle(mostrarOpinions, opinions, 1000);
        // Notificador per persistencia en canvis d'orientacio
        opinionsToggle.setAnimationNotifier(new AnimationNotifier() {

            @Override
            public void onToggled(AnimationToggle animation, boolean toggled) {
                opinionsToggled = toggled;

                if (toggled) {
                    carregarValoracio();
                    carregarOpinions();
                }

                view.invalidate();
            }
        });

        // En mode tablet aquest metode es crida un cop mes del normal, el que
        // provoca
        // que si modifiquem les variables estatiques l'ultim cop que es crida
        // aquestes tenen
        // valors incorrectes. Fem servir dues temporals per evitar canviar les
        // estatiques
        boolean tempInfo = informacioToggled;
        boolean tempOpin = opinionsToggled;

        // Si hem de crearles (no es tracta d'un canvi d'orientacio sino d'una
        // nova view)
        if (createAnimations && savedInstanceState == null) {
            // Marquem que no ha estat activat
            tempInfo = false;
            tempOpin = false;

            if (savedInstanceState == null) {
                informacioToggled = false;
                opinionsToggled = false;
            }
        }

        // Creem les animacions de tamany, i amaguem (ultim parametre) solament
        // si no ha estat activat
        informacioHeightToggle = new HeightToggle(mostrarInfo, informacio, 1000, !tempInfo);
        opinionsHeightToggle = new HeightToggle(mostrarOpinions, opinions, 1000, !tempOpin);

        // Si venim d'un canvi d'orientacio
        if (!createAnimations || savedInstanceState != null) {
            // Si ha estat activat previament, ho apliquem
            if (informacioToggled) {
                informacioToggle.setToggled();
                informacioHeightToggle.setToggled();
            }

            // Si ha estat activat previament, ho apliquem
            if (opinionsToggled) {
                opinionsToggle.setToggled();
                opinionsHeightToggle.setToggled();
            }
        }

        // Possem el metode que carrega elements a l'scroll
        new ScrollLoader(scrollInfoObres, onScrollLoader);

        //Comprobamos que es la primera vez que ejecutamos el fragment
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        boolean firstRun = settings.getBoolean("INFO_FIRST_RUN", true);
        if (firstRun) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("INFO_FIRST_RUN", false);
            editor.commit();

            ShowcaseViews views = new ShowcaseViews(getActivity());
            views.addView(new ShowcaseViews.ItemViewProperties(R.id.imageView_imatge, R.string.tutorial, R.string.tutorial_info));
            views.addView(new ShowcaseViews.ItemViewProperties(R.id.imageView_play, R.string.tutorial, R.string.tutorial_play));
            views.addView(new ShowcaseViews.ItemViewProperties(R.id.imageView_stop, R.string.tutorial, R.string.tutorial_stop));
            views.addView(new ShowcaseViews.ItemViewProperties(R.id.mostrar_opinions, R.string.tutorial, R.string.tutorial_opinions));
            views.addView(new ShowcaseViews.ItemViewProperties(R.id.mostrar_informacio, R.string.tutorial, R.string.tutorial_informacio));
            views.show();
        }


    }

    private void carregarValoracio() {
        API.StartOperation(context, TASK_TYPE.API_GET_RATING, new APINotifier() {

            @Override
            public void onResult(boolean result) {
                if (result) {
                    element.setValoracio(Dades.getLastRating());
                    barraValoracions.setRating(element.getValoracio());
                }
            }
        }, String.valueOf(element.getId()), String.valueOf(tipus.getValue()));
    }

    private void carregarOpinions() {
        boolean executed = false;

        APINotifier notifier = new APINotifier() {

            @Override
            public void onResult(boolean result) {
                if (result) {
                    adapter.addItems(Dades.getComments());
                }
                gridviewComentaris.setLoading(false, false);
            }
        };

        if (adapter.getCount() > 0) {
            Comentari comentari = (Comentari) adapter.getItem(adapter.getCount() - 1);
            if (comentari != null) {
                long last = comentari.getData() / 1000;
                executed = API.StartOperation(context, TASK_TYPE.API_GET_COMMENTS, notifier, String.valueOf(element.getId()), String.valueOf(tipus.getValue()), String.valueOf(last));
            } else {
                Toast.makeText(context, R.string.API_update_error, Toast.LENGTH_SHORT).show();
            }
        } else {
            executed = API.StartOperation(context, TASK_TYPE.API_GET_COMMENTS, notifier, String.valueOf(element.getId()), String.valueOf(tipus.getValue()));
        }

        if (executed) {
            gridviewComentaris.setLoading(true, false);
        }
    }

    public boolean canBeSwitched() {
        return orientationSwitch;
    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.getDefault());

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(context, R.string.fragment_info_tts_language, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(context, R.string.fragment_info_tts_error, Toast.LENGTH_LONG).show();
        }

    }

    PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING || state == TelephonyManager.CALL_STATE_OFFHOOK) {
                if (reproduint) {
                    mPlayer.pause();
                    reproduint = false;
                }
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    };

    @SuppressWarnings("deprecation")
    public void speak_start(String text) {

        HashMap<String, String> myHashRender = new HashMap<String, String>();
        myHashRender.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, element.getDescripcio());

        // Executem paralelament
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

            String state = Environment.getExternalStorageState();
            if (!Environment.MEDIA_MOUNTED.equals(state)) {
                Toast.makeText(context, R.string.toast_tts_error, Toast.LENGTH_LONG).show();
                return;
            }

            destFilename = Environment.getExternalStorageDirectory().getPath() + "/repro.wav";
            tts.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener() {

                @Override
                public void onUtteranceCompleted(String utteranceId) {
                    try {
                        created = true;
                        mPlayer = MediaPlayer.create(context, Uri.parse(destFilename));

                        mPlayer.setOnCompletionListener(new OnCompletionListener() {

                            public void onCompletion(MediaPlayer mp) {
                                reproPlay.setImageResource(R.mipmap.repro_play);
                                reproduint = false;
                                stopped = true;
                            }
                        });


                        mPlayer.start();

                    } catch (Exception ex) {
                        Log.d("Mediaplayer", "ex");

                    }

                }

            });
        } else {
            destFilename = context.getFilesDir().getPath() + "/repro.wav";

            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {

                @Override
                public void onStart(String utteranceId) {
                }

                @Override
                public void onError(String utteranceId) {
                }

                @Override
                public void onDone(String utteranceId) {
                    try {
                        created = true;
                        mPlayer = MediaPlayer.create(context, Uri.parse(destFilename));

                        mPlayer.setOnCompletionListener(new OnCompletionListener() {

                            public void onCompletion(MediaPlayer mp) {
                                reproPlay.setImageResource(R.mipmap.repro_play);
                                reproduint = false;
                                stopped = true;
                            }
                        });
                        mPlayer.start();
                    } catch (Exception ex) {
                        Log.d("Mediaplayer", "ex");
                    }
                }
            });
        }

        //Comprovo quins idiomes te el terminal
        Locale loc[] = Locale.getAvailableLocales();
        boolean choosen = false;
        for (int i = 0; i < loc.length && !choosen; i++) {
            //si algun es espanol
            if (loc[i].toString().length() >= 3 && loc[i].toString().substring(0, 2).equals("es")) {
                if (tts.isLanguageAvailable(loc[i]) != TextToSpeech.LANG_NOT_SUPPORTED) {
                    //si el tts el suporta el trio
                    tts.setLanguage(loc[i]);
                    choosen = true;
                    Log.d("TTS", "Language choosen:" + loc[i].toString());
                }
            }
        }

        Toast.makeText(context, R.string.toast_tts, Toast.LENGTH_LONG).show();
        tts.synthesizeToFile(element.getDescripcio(), myHashRender, destFilename);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlayer != null && reproduint && mPlayer.isPlaying()) {//aixo teoricament ha de solucionar el problema de reproduir en bg.
            mPlayer.pause();
            mPlayer.seekTo(0);
            mPlayer.release();
            reproPlay.setImageResource(R.mipmap.repro_play);
            reproduint = false;
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        ElementImageManager.cancelDownloads();
        API.CancelOperation();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Navigate "up" the demo structure to the launchpad activity.
                // See http://developer.android.com/design/patterns/navigation.html for more.
                Intent i = new Intent(getActivity(), InfoActivity.class);
                NavUtils.navigateUpTo(getActivity(), i);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Aquest metode s'encarrega d' augmentar una imatge a pantalla completa mantenint la seva resolucio i la seva escala.
     * Anima la transicio de pasar de la imatge en miniatura o amb els valors d'escala inicial a la imatge en pantalla completa
     *
     * @param thumbView  La vista en miniatura per agrandar
     * @param view
     * @param imageResId La imatge en format Bitmap que sera representada a pantalla completa
     */
    @SuppressWarnings("deprecation")
    private void zoomImageFromThumb(final View thumbView, Bitmap imatge, View view) {
        // Si hi ha una animacio, la cancela, per tal de continuar amb l'animacio actual
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }
        //Elements de la info
        final LinearLayout lay_museu = (LinearLayout) view.findViewById(R.id.layout_museu);
        final LinearLayout lay_obra = (LinearLayout) view.findViewById(R.id.layout_obra);
        final LinearLayout lay_info = (LinearLayout) view.findViewById(R.id.contenidor_informacio);
        final LinearLayout lay_coments = (LinearLayout) view.findViewById(R.id.contenidor_opinions);
        final RelativeLayout lay_repro = (RelativeLayout) view.findViewById(R.id.layout_repro);
        final Button info = (Button) view.findViewById(R.id.mostrar_informacio);
        final Button opinions = (Button) view.findViewById(R.id.mostrar_opinions);

        //Fa invisibles els elements de la info
        ViewHelper.setAlpha(lay_museu, 0);
        ViewHelper.setAlpha(lay_obra, 0);
        ViewHelper.setAlpha(lay_repro, 0);
        ViewHelper.setAlpha(lay_info, 0);
        ViewHelper.setAlpha(lay_coments, 0);
        ViewHelper.setAlpha(info, 0);
        ViewHelper.setAlpha(opinions, 0);
        // Obte el imageView a pantalla completa i li coloca la imatge
        expandedImageView.setImageBitmap(imatge);

        // Calcula els limits inicials i finals de la imatge al fer el zoom
        // involves lots of math. Yay, math.
        startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        //Els limits de inici son el rectangle de la imatge en miniatura
        //Els limits finals son el rectangle de la imatge a pantalla completa
        //Estableix el contenedor de la imatge expandida i l'origen i el final de les propietats d'animacio (X, Y)
        thumbView.getGlobalVisibleRect(startBounds);
        getActivity().findViewById(R.id.container).getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        //Ajusta els limits finals amb el mateix aspecte (escala) que els limits inicials (imatge en miniatura)
        //centra la imatge per evitar que s'estiri durant l'animacio
        //Calcula el factor d'escala al comencament de l'animacio

        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extendeix els limits d'inici horitzontalment
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extendeix els limits d'inici verticalment
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        startBounds.left = display.getWidth() / 2 - finalBounds.width() / 2;
        startBounds.top = thumbView.getTop() - thumbView.getHeight();

        //Oculta la imatge en miniatura i fa visible la imatge expandida
        ViewHelper.setAlpha(thumbView, 0);
        expandedImageView.setVisibility(View.VISIBLE);

        //Estableix els punts X, Y de transformacio a la imatge expandida
        AnimatorProxy.wrap(expandedImageView).setPivotX(0.5f);
        AnimatorProxy.wrap(expandedImageView).setPivotY(0.5f);

        //Construeix i executa l'animacio de les cuatre propietats ((X, Y, SCALE_X, ai SCALE_Y) --> ho fa a la vegada
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, "x", startBounds.left,
                        finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, "y", startBounds.top,
                        finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, "scaleX", startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView, "scaleY", startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        expandActivat = true;

        // Expliquem com tornar enrere
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        boolean firstRun = settings.getBoolean("ZOOM_FIRST_RUN", true);
        if (firstRun) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("ZOOM_FIRST_RUN", false);
            editor.commit();

            ShowcaseViews views = new ShowcaseViews(getActivity());
            views.addView(new ShowcaseViews.ItemViewProperties(R.id.expanded_image, R.string.tutorial, R.string.tutorial_zoom));
            views.show();
        }
    }

    //Metode que anima la imatge expandida per pasar a la imatge en miniatura de la info, fa visible els elements de la info
    public void returnToInfo() {
        //Elements de la info
        final LinearLayout lay_museu = (LinearLayout) activity.findViewById(R.id.layout_museu);
        final LinearLayout lay_obra = (LinearLayout) activity.findViewById(R.id.layout_obra);
        final LinearLayout lay_info = (LinearLayout) activity.findViewById(R.id.contenidor_informacio);
        final LinearLayout lay_coments = (LinearLayout) activity.findViewById(R.id.contenidor_opinions);
        final RelativeLayout lay_repro = (RelativeLayout) activity.findViewById(R.id.layout_repro);
        final Button info = (Button) activity.findViewById(R.id.mostrar_informacio);
        final Button opinions = (Button) activity.findViewById(R.id.mostrar_opinions);

        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }
        float startScaleFinal = startScale;
        final ImageView thumbView = (ImageView) activity.findViewById(R.id.imageView_imatge);

        AnimatorProxy.wrap(expandedImageView).setPivotX(0.5f);
        AnimatorProxy.wrap(expandedImageView).setPivotY(0.5f);

        //Anima les propietats de la imatge (X,Y,escala) quan torna a miniatura, als valors originals
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, "x", startBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, "y", startBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, "scaleX", startScaleFinal))
                .with(ObjectAnimator.ofFloat(expandedImageView, "scaleY", startScaleFinal));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ViewHelper.setAlpha(thumbView, 1);
                expandedImageView.clearAnimation();
                expandedImageView.setVisibility(View.INVISIBLE);
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                ViewHelper.setAlpha(thumbView, 1);
                expandedImageView.clearAnimation();
                expandedImageView.setVisibility(View.INVISIBLE);
                mCurrentAnimator = null;
            }
        });
        set.start();

        mCurrentAnimator = set;
        //Fa visibles els elements de la info
        ViewHelper.setAlpha(lay_museu, 1);
        ViewHelper.setAlpha(lay_obra, 1);
        ViewHelper.setAlpha(lay_repro, 1);
        ViewHelper.setAlpha(lay_info, 1);
        ViewHelper.setAlpha(lay_coments, 1);
        ViewHelper.setAlpha(info, 1);
        ViewHelper.setAlpha(opinions, 1);
        expandActivat = false;
    }

    public boolean imageExpanded() {
        return expandActivat;
    }

    RatingBar.OnTouchListener onValoracio = new RatingBar.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!Usuari.get().isLogged()) {
                if (!registratToast) {
                    Toast.makeText(context, R.string.fragment_info_register_stars, Toast.LENGTH_LONG).show();
                    registratToast = true;
                }
                return true;
            }

            barraValoracions.setIsIndicator(true);
            if (event.getAction() == MotionEvent.ACTION_UP) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(activity);

                LinearLayout lay = new LinearLayout(activity);
                final RatingBar rating_dialog = new RatingBar(activity);
                final TextView text_dialog = new TextView(activity);

                rating_dialog.setNumStars(5);
                rating_dialog.setStepSize((float) 0.5);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

                rating_dialog.setLayoutParams(lp);
                text_dialog.setLayoutParams(lp);
                text_dialog.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);


                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                lay.setLayoutParams(params);
                lay.setOrientation(LinearLayout.VERTICAL);
                lay.addView(rating_dialog);
                lay.addView(text_dialog);
                lay.setGravity(Gravity.CENTER);

                dialog.setMessage(R.string.fragment_info_editar_valoracio);
                dialog.setCancelable(false);
                dialog.setView(lay);

                rating_dialog.setRating(barraValoracions.getRating());
                text_dialog.setText(R.string.fragment_info_click_valorar);

                rating_dialog.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                        Resources resources = getResources();
                        String valors[] = resources.getStringArray(R.array.fragment_info_valoracions);
                        text_dialog.setText(valors[(int) (rating - 0.5)]);
                    }
                });

                dialog.setPositiveButton(R.string.fragment_info_valorar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        API.StartOperation(context, TASK_TYPE.API_RATE, new APINotifier() {

                            @Override
                            public void onResult(boolean result) {
                                if (result) {
                                    barraValoracions.setRating(Dades.getLastRating());
                                }
                            }
                        }, String.valueOf(element.getId()), String.valueOf(tipus.getValue()), String.valueOf(rating_dialog.getRating()));
                    }
                });
                dialog.setNegativeButton(R.string.dialog_cancelar, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                    }
                });
                dialog.show();
            }
            return true;
        }
    };

    OnClickListener onComment = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // Si no estem carregant
            if (!adapter.isLoading()) {
                // Si no esta buida
                if (editorComentari.getText().toString().trim().length() != 0) {
                    String comentari = editorComentari.getText().toString();
                    editorComentari.setText("");

                    gridviewComentaris.setLoading(true, true);

                    API.StartOperation(context, TASK_TYPE.API_COMMENT, new APINotifier() {

                        @Override
                        public void onResult(boolean result) {
                            // Si tot ha anat be
                            if (result) {
                                adapter.addItem(0, Usuari.get().getUltimComentari());
                                // No cridem notifyDataSetChanged, ho fara el setloading
                            }
                            // Treiem l'icono de carregar
                            gridviewComentaris.setLoading(false, true);
                        }
                    }, String.valueOf(element.getId()), String.valueOf(tipus.getValue()), comentari);
                } else {
                    Toast.makeText(context, R.string.fragment_info_comentar, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, R.string.fragment_info_carregant, Toast.LENGTH_SHORT).show();
            }
        }
    };

    OnClickListener onClickPlay = new OnClickListener() {
        @Override
        public void onClick(View v) {

            if (reproduint) {
                if (mPlayer != null) {
                    mPlayer.pause();
                }
                reproPlay.setImageResource(R.mipmap.repro_play);
                reproduint = false;
            } else {
                if (!created) speak_start(textInformacio.getText().toString());
                else {
                    if (stopped) {
                        mPlayer.seekTo(0);
                    }
                    mPlayer.start();

                }
                reproPlay.setImageResource(R.mipmap.repro_pause);
                reproduint = true;
                stopped = false;
            }
        }
    };

    OnTouchListener onTouchPlay = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // Canvia la imatge que mostra per donar la sensacio de clicat
            // Retornem sempre false per indicar que no hem acabat la gestio
            // (onClick)

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (reproduint)
                        reproPlay.setImageResource(R.mipmap.repro_pause_clicked);
                    else
                        reproPlay.setImageResource(R.mipmap.repro_play_clicked);
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (reproduint)
                        reproPlay.setImageResource(R.mipmap.repro_pause);
                    else
                        reproPlay.setImageResource(R.mipmap.repro_play);
                    break;
            }
            return false;
        }
    };

    OnClickListener onClickStop = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (created) {
                tts.stop();
                reproPlay.setImageResource(R.mipmap.repro_play);
                reproduint = false;
                mPlayer.pause();
                stopped = true;
            }
        }
    };

    OnTouchListener onTouchStop = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // Canvia la imatge que mostra per donar la sensacio de clicat
            // Retornem sempre false per indicar que no hem acabat la gestio
            // (onClick)

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    reproStop.setImageResource(R.mipmap.repro_stop_clicked);
                    break;

                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    reproStop.setImageResource(R.mipmap.repro_stop);
                    break;
            }
            return false;
        }
    };

    OnClickListener onClickMuseu = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!Utils.isExpandedMode(getActivity())) {
                Intent i = new Intent(getActivity(), InfoActivity.class);
                i.putExtra("id", element.getMuseuId());
                i.putExtra("tipus", TIPUS_ELEMENT.TIPUS_MUSEU);
                startActivityForResult(i, InfoFragment.ACTIVITY_CREATE);
            } else {
                ViewPager pager = (ViewPager) getActivity().findViewById(R.id.main_pager);
                ScreenSlidePagerAdapter adapter = (ScreenSlidePagerAdapter) pager.getAdapter();

                new AlphaBoth(null, pager, 500).execute();


                Museu museu = Dades.getMuseu(((Obra) element).getMuseuId());
                Fragment fragment = InfoFragment.newInstance(museu);
                adapter.customize(fragment);
                pager.setCurrentItem(1, true);
            }
        }
    };

    OnClickListener onClickAutor = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!Utils.isExpandedMode(getActivity())) {
                Intent i = new Intent(getActivity(), InfoActivity.class);
                i.putExtra("id", ((Obra) element).getAutorId());
                i.putExtra("tipus", TIPUS_ELEMENT.TIPUS_AUTOR);
                startActivityForResult(i, InfoFragment.ACTIVITY_CREATE);
            } else {
                ViewPager pager = (ViewPager) getActivity().findViewById(R.id.main_pager);
                ScreenSlidePagerAdapter adapter = (ScreenSlidePagerAdapter) pager.getAdapter();

                new AlphaBoth(null, pager, 500).execute();

                Autor autor = Dades.getAutor(((Obra) element).getAutorId());
                Fragment fragment = InfoFragment.newInstance(autor);
                adapter.customize(fragment);
                pager.setCurrentItem(1, true);
            }
        }
    };

    OnClickListener onClickTelefon = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            llamar(((Museu) element).getTelefon());
        }

        private void llamar(String telefon) {
            try {
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + telefon)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    OnClickListener onClickDireccio = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(getActivity(), MapsActivity.class);
            i.putExtra("Latitud", ((Museu) element).getLatitud());
            i.putExtra("Longitud", ((Museu) element).getLongitud());
            startActivityForResult(i, InfoFragment.ACTIVITY_CREATE);
        }
    };

    ScrollLoaderListener onScrollLoader = new ScrollLoaderListener() {

        @Override
        public boolean isEnabled() {
            // Solament ho activem si estem mostrant els comentaris
            return opinionsToggled && !opinionsHeightToggle.isRunning();
        }

        @Override
        public void onEventDown() {
            // Mostrem els textos d'ajuda
            upForMore.setVisibility(View.VISIBLE);
            upClick.setVisibility(View.GONE);
        }

        @Override
        public void onEventMove(float alphaY, boolean carregarElements) {
            // Si tenim desplacament positiu
            if (alphaY > 0) {
                // Mostrem els elements d'ajuda
                loadMore.setVisibility(View.VISIBLE);
                loadMoreImg.getLayoutParams().height = (int) (alphaY / 4);
                loadMoreImg.requestLayout();
            } else {
                // Amaguem els elements d'ajuda
                loadMore.setVisibility(View.GONE);
            }

            // Si ja es poden carregar
            if (carregarElements) {
                // Mostrem l'ajuda de que es pot deixar anar
                upForMore.setVisibility(View.GONE);
                upClick.setVisibility(View.VISIBLE);
            } else {
                // Mostrem l'ajuda de pujar
                upForMore.setVisibility(View.VISIBLE);
                upClick.setVisibility(View.GONE);
            }
        }


        @Override
        public void onEventUp(float alphaY, boolean carregarElements) {
            // Si cal carregar nous elements
            if (carregarElements) {
                carregarOpinions();
            }

            // Restrablim les variables
            loadMore.setVisibility(View.GONE);
        }
    };


}


