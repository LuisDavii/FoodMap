from rest_framework import serializers
from django.contrib.auth.models import User
from .models import AlimentoPlanoAlimentar

class UsuarioSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ['id', 'username', 'email', 'password']
        extra_kwargs = {
            'password': {'write_only': True}
        }

    def create(self, validated_data):
        user = User.objects.create_user(
            username=validated_data['username'],
            email=validated_data['email'],
            password=validated_data['password']
        )
        return user
    
class RefeicaoSerializer(serializers.ModelSerializer):
    class Meta:
        model = AlimentoPlanoAlimentar
        fields = ['id', 'usuario', 'dia_semana', 'tipo_refeicao', 'descricao', 'calorias', 'concluido']