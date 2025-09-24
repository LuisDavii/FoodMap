from rest_framework import serializers
from .models import Usuario
from django.contrib.auth.hashers import make_password

class UsuarioSerializer(serializers.ModelSerializer):
    class Meta:
        model = Usuario
        fields = ['id', 'userName', 'name', 'email', 'password']

    def create(self, validated_data):
        # Hash da senha
        validated_data['password'] = make_password(validated_data['password'])
        return super().create(validated_data)
