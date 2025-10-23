package curso.petenusso.clinicsapp.ui.medico.dialogs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import curso.petenusso.clinicsapp.databinding.ItemProntuarioBinding
import curso.petenusso.clinicsapp.model.prontuario.Prontuario

class ProntuarioAdapter(private val prontuarios: List<Prontuario>): RecyclerView.Adapter<ProntuarioAdapter.ViewHolder>(){

    inner class ViewHolder(val binding: ItemProntuarioBinding) :
            RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProntuarioBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }


    override fun getItemCount(): Int = prontuarios.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = prontuarios[position]
        holder.binding.apply {
            textData.text = "📅 ${p.dataCadastro}"
            textAtendimento.text = "🩺 Atendimento: ${p.atendimento}"
            textMedicacao.text = "💊 Medicação: ${p.medicacao}"
            textAlergias.text = "⚕️ Alergias: ${p.alergias}"
            textDeficiencia.text = "♿ Deficiência: ${p.deficiencia}"
            textComorbidade.text = "🧬 Comorbidade: ${p.comorbidade}"
            textExames.text = "🔬 Exames: ${p.exames}"
        }
    }

}