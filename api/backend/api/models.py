from django.db import models
from django.contrib.auth.models import User

class AlimentoPlanoAlimentar(models.Model):

    usuario = models.ForeignKey(User, on_delete=models.CASCADE)
    
    dia_semana = models.CharField(max_length=20)
    tipo_refeicao = models.CharField(max_length=20)
    descricao = models.TextField()
    calorias = models.IntegerField()
    concluido = models.BooleanField(default=False)

    class Meta:
        db_table = 'alimentos_planoalimentar'
    
    def __str__(self):
        return f"{self.descricao} - {self.dia_semana}"