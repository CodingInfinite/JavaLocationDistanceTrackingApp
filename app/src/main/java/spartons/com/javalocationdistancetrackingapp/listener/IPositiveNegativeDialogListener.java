package spartons.com.javalocationdistancetrackingapp.listener;

@FunctionalInterface
public interface IPositiveNegativeDialogListener {

    void onPositiveClick();

    default void onNegativeClick() {

    }
}
